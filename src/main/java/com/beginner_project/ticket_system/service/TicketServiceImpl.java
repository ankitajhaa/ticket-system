package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dao.TicketRepository;
import com.beginner_project.ticket_system.dto.TicketCreateRequest;
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
    public Ticket createTicket(TicketCreateRequest request, Users user) {

        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                Status.OPEN,
                user
        );

        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getTicketsForUser(Users user) {

        if (user.getRole() == Role.ADMIN) {
            return ticketRepository.findAll();
        }

        if (user.getRole() == Role.SUPPORT_AGENT) {
            return ticketRepository.findByAssignedAgent(user);
        }

        return ticketRepository.findByCreatedBy(user);
    }

    @Override
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }
}