package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.dto.TicketResponse;
import com.beginner_project.ticket_system.dto.TicketUpdateRequest;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.service.TicketService;
import com.beginner_project.ticket_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    public TicketController(
            TicketService ticketService,
            UserService userService
    ) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    // ===== CREATE TICKET =====

    @PostMapping
    public TicketResponse createTicket(
            @Valid @RequestBody TicketCreateRequest request
    ) {

        Users user = getCurrentUser();

        return ticketService.createTicket(request, user);
    }

    // ===== GET ALL TICKETS (ROLE-BASED INSIDE SERVICE) =====

    @GetMapping
    public List<TicketResponse> getTickets(
            @RequestParam(required = false) String status
    ) {

        Users user = getCurrentUser();

        return ticketService.getTicketsForUser(user, status);
    }

    // ===== GET SINGLE TICKET =====

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {

        Users user = getCurrentUser();

        return ticketService.getTicketById(id, user);
    }

    // ===== UPDATE TICKET =====

    @PatchMapping("/{id}")
    public TicketResponse updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request
    ) {

        Users user = getCurrentUser();

        return ticketService.updateTicket(id, request, user);
    }

    // ===== HELPER =====

    private Users getCurrentUser() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userService.getByUsername(username);
    }
}