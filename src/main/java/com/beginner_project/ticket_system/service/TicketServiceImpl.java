package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dao.TicketRepository;
import com.beginner_project.ticket_system.dao.UserRepository;
import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.dto.TicketResponse;
import com.beginner_project.ticket_system.dto.TicketUpdateRequest;
import com.beginner_project.ticket_system.dto.UserResponse;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TicketResponse createTicket(TicketCreateRequest request, Users user) {

        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                Status.OPEN,
                user
        );

        Ticket saved = ticketRepository.save(ticket);

        return mapTicket(saved);
    }

    @Override
    public List<TicketResponse> getTicketsForUser(
            Users user,
            String status
    ) {

        Status statusEnum = null;

        // ===== STATUS VALIDATION =====
        if (status != null) {
            try {
                statusEnum = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new BusinessException("Invalid status value");
            }
        }

        List<Ticket> tickets;

        // ===== ROLE FILTERING =====
        if (user.getRole() == Role.ADMIN) {

            tickets = (statusEnum == null)
                    ? ticketRepository.findAll()
                    : ticketRepository.findByStatus(statusEnum);

        } else if (user.getRole() == Role.SUPPORT_AGENT) {

            tickets = (statusEnum == null)
                    ? ticketRepository.findByAssignedAgent(user)
                    : ticketRepository
                    .findByAssignedAgentAndStatus(user, statusEnum);

        } else {

            tickets = (statusEnum == null)
                    ? ticketRepository.findByCreatedBy(user)
                    : ticketRepository
                    .findByCreatedByAndStatus(user, statusEnum);
        }

        return tickets.stream()
                .map(this::mapTicket)
                .toList();
    }

    @Override
    public TicketResponse getTicketById(Long id, Users user) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        // ADMIN can view everything
        if (user.getRole() == Role.ADMIN) {
            return mapTicket(ticket);
        }

        // SUPPORT_AGENT can view only assigned tickets
        if (user.getRole() == Role.SUPPORT_AGENT) {
            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(user.getId())) {
                throw new BusinessException("Access denied");
            }
            return mapTicket(ticket);
        }

        // CUSTOMER → only own ticket
        if (!ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException("Access denied");
        }

        return mapTicket(ticket);
    }

    @Override
    public TicketResponse assignToSelf(Long ticketId, Users agent) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        if (ticket.getStatus() == Status.CLOSE) {
            throw new BusinessException("Closed ticket cannot be modified");
        }

        if (ticket.getAssignedAgent() != null) {
            throw new BusinessException("Ticket already assigned");
        }

        ticket.setAssignedAgent(agent);
        ticket.setStatus(Status.ASSIGNED);

        Ticket saved = ticketRepository.save(ticket);

        return mapTicket(saved);
    }

    @Override
    public TicketResponse updateTicket(
            Long ticketId,
            TicketUpdateRequest request,
            Users user
    ) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new BusinessException("Ticket does not exist"));

        // CLOSED ticket check
        if (ticket.getStatus() == Status.CLOSE) {
            throw new BusinessException(
                    "Closed tickets cannot be modified"
            );
        }

        String action = request.getAction().toLowerCase();

        switch (action) {

            // Claim
            case "claim" -> {

                if (user.getRole() != Role.SUPPORT_AGENT) {
                    throw new BusinessException(
                            "You are not allowed to perform this action");
                }

                if (ticket.getAssignedAgent() != null) {
                    throw new BusinessException("Ticket already assigned");
                }

                ticket.setAssignedAgent(user);
                ticket.setStatus(Status.ASSIGNED);
            }

            // UPDATE PROGRESS
            case "update_progress" -> {

                if (user.getRole() != Role.SUPPORT_AGENT ||
                        ticket.getAssignedAgent() == null ||
                        !ticket.getAssignedAgent().getId()
                                .equals(user.getId())) {

                    throw new BusinessException(
                            "You are not allowed to perform this action");
                }

                if (request.getStatus() == null) {
                    throw new BusinessException("Invalid request data");
                }

                Status newStatus;

                try {
                    newStatus =
                            Status.valueOf(request.getStatus().toUpperCase());
                } catch (Exception e) {
                    throw new BusinessException("Invalid request data");
                }

                ticket.setStatus(newStatus);
            }

            // ADMIN ASSIGN
            case "assign" -> {

                if (user.getRole() != Role.ADMIN) {
                    throw new BusinessException(
                            "You are not allowed to perform this action");
                }

                if (request.getAssignedAgent() == null) {
                    throw new BusinessException("Invalid request data");
                }

                Users agent = userRepository.findById(
                        request.getAssignedAgent()
                ).orElseThrow(() ->
                        new BusinessException("Agent not found"));

                ticket.setAssignedAgent(agent);
                ticket.setStatus(Status.ASSIGNED);
            }

            default -> throw new BusinessException("Invalid request data");
        }

        Ticket saved = ticketRepository.save(ticket);

        return mapTicket(saved);
    }

    private TicketResponse mapTicket(Ticket ticket) {

        TicketResponse response = new TicketResponse();

        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus().name());
        response.setCreatedBy(mapUser(ticket.getCreatedBy()));
        response.setAssignedAgent(mapUser(ticket.getAssignedAgent()));
        response.setCreatedAt(ticket.getCreatedAt());

        return response;
    }

    private UserResponse mapUser(Users user) {

        if (user == null) return null;

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}