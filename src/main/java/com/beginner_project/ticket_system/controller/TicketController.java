package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    //CREATE TICKET
    @PostMapping
    public Ticket createTicket(@Valid @RequestBody TicketCreateRequest request) {
        Users user = (Users) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return ticketService.createTicket(request, user);
    }

    //VIEW TICKETS
    @GetMapping
    public List<Ticket> getTickets() {
        Users user = (Users) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ticketService.getTicketsForUser(user);
    }

    //VIEW SINGLE TICKET
    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable long id) {
        return ticketService.getTicketById(id);
    }

    @PatchMapping("/{id}/claim")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<?> claimTicket(@PathVariable Long id) {

        Users agent = (Users)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        Ticket ticket = ticketService.assignToSelf(id, agent);

        return ResponseEntity.ok(ticket);
    }
}
