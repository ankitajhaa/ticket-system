package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dao.TicketRepository;
import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.dto.TicketResponse;
import com.beginner_project.ticket_system.dto.UserResponse;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.enums.Status;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
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
                throw new RuntimeException("Invalid status value");
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
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // ADMIN can view everything
        if (user.getRole() == Role.ADMIN) {
            return mapTicket(ticket);
        }

        // SUPPORT_AGENT can view only assigned tickets
        if (user.getRole() == Role.SUPPORT_AGENT) {
            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }
            return mapTicket(ticket);
        }

        // CUSTOMER → only own ticket
        if (!ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapTicket(ticket);
    }

    @Override
    public TicketResponse assignToSelf(Long ticketId, Users agent) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() == Status.CLOSE) {
            throw new RuntimeException("Closed ticket cannot be modified");
        }

        if (ticket.getAssignedAgent() != null) {
            throw new RuntimeException("Ticket already assigned");
        }

        ticket.setAssignedAgent(agent);
        ticket.setStatus(Status.ASSIGNED);

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