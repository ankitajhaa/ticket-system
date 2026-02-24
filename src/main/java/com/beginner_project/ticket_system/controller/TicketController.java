package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.dto.TicketResponse;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.service.TicketService;
import com.beginner_project.ticket_system.service.UserService;
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

    private final UserService userService;

    public TicketController(
            TicketService ticketService,
            UserService userService
    ) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public TicketResponse createTicket(
            @Valid @RequestBody TicketCreateRequest request
    ) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Users user = userService.getByUsername(username);

        return ticketService.createTicket(request, user);
    }

    @GetMapping
    public List<TicketResponse> getTickets(
            @RequestParam(required = false) String status
    ) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Users user = userService.getByUsername(username);

        return ticketService.getTicketsForUser(user, status);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Users user = userService.getByUsername(username);

        return ticketService.getTicketById(id, user);
    }

    @PatchMapping("/{id}/claim")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<TicketResponse> claimTicket(
            @PathVariable Long id
    ) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Users agent = userService.getByUsername(username);

        return ResponseEntity.ok(
                ticketService.assignToSelf(id, agent)
        );
    }

}