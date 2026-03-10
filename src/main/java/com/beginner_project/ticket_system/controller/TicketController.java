package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.dto.TicketFilterRequest;
import com.beginner_project.ticket_system.dto.TicketResponse;
import com.beginner_project.ticket_system.dto.TicketUpdateRequest;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Priority;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.service.TicketService;
import com.beginner_project.ticket_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    public TicketController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @PostMapping
    public TicketResponse createTicket(@Valid @RequestBody TicketCreateRequest request) {
        return ticketService.createTicket(request, getCurrentUser());
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assignedAgentId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Boolean slaBreached,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        TicketFilterRequest filter = new TicketFilterRequest();

        if (status != null) {
            try {
                filter.setStatus(Status.valueOf(status.toUpperCase()));
            } catch (Exception e) {
                throw new BusinessException("Invalid status value", HttpStatus.BAD_REQUEST);
            }
        }

        if (priority != null) {
            try {
                filter.setPriority(Priority.valueOf(priority.toUpperCase()));
            } catch (Exception e) {
                throw new BusinessException("Invalid priority value", HttpStatus.BAD_REQUEST);
            }
        }

        filter.setAssignedAgentId(assignedAgentId);
        filter.setCustomerId(customerId);
        filter.setSlaBreached(slaBreached);
        filter.setCreatedFrom(createdFrom);
        filter.setCreatedTo(createdTo);
        filter.setDeadlineFrom(deadlineFrom);
        filter.setDeadlineTo(deadlineTo);

        return ResponseEntity.ok(ticketService.searchTickets(
                filter, page, size, sortBy, sortDir, getCurrentUser()
        ));
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicketById(id, getCurrentUser());
    }

    @PatchMapping("/{id}")
    public TicketResponse updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request
    ) {
        return ticketService.updateTicket(id, request, getCurrentUser());
    }

    private Users getCurrentUser() {
        return (Users) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}