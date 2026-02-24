package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dao.AuditLogRepository;
import com.beginner_project.ticket_system.dao.TicketRepository;
import com.beginner_project.ticket_system.dao.UserRepository;
import com.beginner_project.ticket_system.dto.*;
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
    private final AuditLogRepository auditLogRepository;

    public TicketServiceImpl(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public TicketResponse createTicket(TicketCreateRequest request, Users user) {

        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                Status.OPEN,
                user
        );

        return mapTicket(ticketRepository.save(ticket));
    }

    @Override
    public List<TicketResponse> getTicketsForUser(Users user, String status) {

        Status statusEnum = null;

        if (status != null) {
            try {
                statusEnum = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new BusinessException("Invalid status value");
            }
        }

        List<Ticket> tickets;

        if (user.getRole() == Role.ADMIN) {

            tickets = (statusEnum == null)
                    ? ticketRepository.findAll()
                    : ticketRepository.findByStatus(statusEnum);

        } else if (user.getRole() == Role.SUPPORT_AGENT) {

            tickets = (statusEnum == null)
                    ? ticketRepository.findByAssignedAgent(user)
                    : ticketRepository.findByAssignedAgentAndStatus(user, statusEnum);

        } else {

            tickets = (statusEnum == null)
                    ? ticketRepository.findByCreatedBy(user)
                    : ticketRepository.findByCreatedByAndStatus(user, statusEnum);
        }

        return tickets.stream().map(this::mapTicket).toList();
    }

    @Override
    public TicketResponse getTicketById(Long id, Users user) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        if (user.getRole() == Role.SUPPORT_AGENT) {
            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(user.getId())) {

                throw new BusinessException(
                        "You do not have permission to view this ticket");
            }
        }

        if (user.getRole() == Role.CUSTOMER &&
                !ticket.getCreatedBy().getId().equals(user.getId())) {

            throw new BusinessException(
                    "You do not have permission to view this ticket");
        }

        TicketResponse response = mapTicket(ticket);

        if (user.getRole() == Role.ADMIN ||
                user.getRole() == Role.SUPPORT_AGENT) {

            List<AuditLogResponse> logs =
                    auditLogRepository.findByTicket(ticket)
                            .stream()
                            .map(a -> new AuditLogResponse(
                                    a.getId(),
                                    a.getUpdated_by().getUsername(),
                                    a.getAction(),
                                    a.getTimestamp()
                            ))
                            .toList();

            response.setAuditLogs(logs);
        }

        return response;
    }

    @Override
    public TicketResponse assignToSelf(Long ticketId, Users agent) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        if (ticket.getStatus() == Status.CLOSED) {
            throw new BusinessException("Closed ticket cannot be modified");
        }

        if (ticket.getAssignedAgent() != null) {
            throw new BusinessException("Ticket already assigned");
        }

        ticket.setAssignedAgent(agent);
        ticket.setStatus(Status.ASSIGNED);

        return mapTicket(ticketRepository.save(ticket));
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

        if (ticket.getStatus() == Status.CLOSED) {
            throw new BusinessException("Closed tickets cannot be modified");
        }

        String action = request.getAction().toLowerCase();

        switch (action) {

            case "claim" -> {

                if (user.getRole() != Role.SUPPORT_AGENT)
                    throw new BusinessException("You are not allowed to perform this action");

                if (ticket.getAssignedAgent() != null)
                    throw new BusinessException("Ticket already assigned");

                ticket.setAssignedAgent(user);
                ticket.setStatus(Status.ASSIGNED);
            }

            case "update_progress" -> {

                boolean isAssignedAgent =
                        ticket.getAssignedAgent() != null &&
                                ticket.getAssignedAgent().getId().equals(user.getId());

                if (user.getRole() != Role.ADMIN &&
                        !(user.getRole() == Role.SUPPORT_AGENT && isAssignedAgent)) {

                    throw new BusinessException("You are not allowed to perform this action");
                }

                if (request.getStatus() == null)
                    throw new BusinessException("Invalid request data");

                Status newStatus;

                try {
                    newStatus = Status.valueOf(request.getStatus().toUpperCase());
                } catch (Exception e) {
                    throw new BusinessException("Invalid request data");
                }

                ticket.setStatus(newStatus);
            }

            case "assign" -> {

                if (user.getRole() != Role.ADMIN)
                    throw new BusinessException("You are not allowed to perform this action");

                if (request.getAssignedAgent() == null)
                    throw new BusinessException("Invalid request data");

                Users agent = userRepository.findById(
                        request.getAssignedAgent()
                ).orElseThrow(() ->
                        new BusinessException("Agent not found"));

                ticket.setAssignedAgent(agent);
                ticket.setStatus(Status.ASSIGNED);
            }

            default -> throw new BusinessException("Invalid request data");
        }

        return mapTicket(ticketRepository.save(ticket));
    }

    // ===== MAPPERS =====

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