package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.enums.Action;
import com.beginner_project.ticket_system.repository.AuditLogRepository;
import com.beginner_project.ticket_system.repository.TicketRepository;
import com.beginner_project.ticket_system.repository.UserRepository;
import com.beginner_project.ticket_system.dto.*;
import com.beginner_project.ticket_system.entity.AuditLog;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.exception.BusinessException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

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

    // ================= CREATE =================

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request, Users user) {

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Admins cannot create tickets", HttpStatus.FORBIDDEN);
        }

        logger.info("Creating ticket by user: {}", user.getUsername());

        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                Status.OPEN,
                user
        );

        return mapTicket(ticketRepository.save(ticket));
    }

    // ================= LIST =================

    @Override
    public List<TicketResponse> getTicketsForUser(Users user, String status) {

        Status statusEnum = null;

        if (status != null) {
            try {
                statusEnum = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new BusinessException("Invalid status value", HttpStatus.BAD_REQUEST);
            }
        }

        logger.info("Fetching tickets for user: {} with role: {}", user.getUsername(), user.getRole());

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

    // ================= GET BY ID =================

    @Override
    public TicketResponse getTicketById(Long id, Users user) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Ticket not found", HttpStatus.NOT_FOUND));

        if (user.getRole() == Role.SUPPORT_AGENT) {
            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(user.getId())) {
                throw new BusinessException(
                        "You do not have permission to view this ticket", HttpStatus.FORBIDDEN);
            }
        }

        if (user.getRole() == Role.CUSTOMER &&
                !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You do not have permission to view this ticket", HttpStatus.FORBIDDEN);
        }

        logger.info("Fetching ticket {} for user {}", id, user.getUsername());

        TicketResponse response = mapTicket(ticket);

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPPORT_AGENT) {

            List<AuditLogResponse> logs =
                    auditLogRepository.findByTicket(ticket)
                            .stream()
                            .map(a -> new AuditLogResponse(
                                    a.getId(),
                                    a.getUpdatedBy().getUsername(),
                                    a.getAction(),
                                    a.getTimestamp()
                            ))
                            .toList();

            response.setAuditLogs(logs);
        }

        return response;
    }

    // ================= UPDATE =================

    @Override
    @Transactional
    public TicketResponse updateTicket(
            Long ticketId,
            TicketUpdateRequest request,
            Users user
    ) {

        logger.info("User {} performing {} on ticket {}",
                user.getUsername(), request.getAction(), ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new BusinessException("Ticket does not exist", HttpStatus.NOT_FOUND));

        if (ticket.getStatus() == Status.CLOSED)
            throw new BusinessException("Closed tickets cannot be modified", HttpStatus.CONFLICT);

        Action action = request.getAction();
        String oldValue = ticket.getStatus().name();

        switch (action) {

            case CLAIM -> {
                if (user.getRole() != Role.SUPPORT_AGENT)
                    throw new BusinessException("You are not allowed", HttpStatus.FORBIDDEN);

                if (ticket.getAssignedAgent() != null)
                    throw new BusinessException("Ticket already assigned", HttpStatus.CONFLICT);

                ticket.setAssignedAgent(user);
                ticket.setStatus(Status.ASSIGNED);
            }

            case UPDATE_PROGRESS -> {

                boolean isAssignedAgent =
                        ticket.getAssignedAgent() != null &&
                                ticket.getAssignedAgent().getId().equals(user.getId());

                if (user.getRole() != Role.ADMIN &&
                        !(user.getRole() == Role.SUPPORT_AGENT && isAssignedAgent)) {
                    throw new BusinessException("You are not allowed", HttpStatus.FORBIDDEN);
                }

                if (request.getStatus() == null)
                    throw new BusinessException("Status is required for UPDATE_PROGRESS", HttpStatus.BAD_REQUEST);

                Status newStatus;
                try {
                    newStatus = Status.valueOf(request.getStatus().toUpperCase());
                } catch (Exception e) {
                    throw new BusinessException("Invalid status value", HttpStatus.BAD_REQUEST);
                }

                ticket.setStatus(newStatus);
            }

            case ASSIGN -> {

                if (user.getRole() != Role.ADMIN)
                    throw new BusinessException(
                            "You are not allowed to perform this action", HttpStatus.FORBIDDEN);

                if (request.getAssignedAgent() == null)
                    throw new BusinessException("Agent id is required", HttpStatus.BAD_REQUEST);

                Users agent = userRepository.findById(request.getAssignedAgent())
                        .orElseThrow(() ->
                                new BusinessException("Agent not found", HttpStatus.NOT_FOUND));

                ticket.setAssignedAgent(agent);
                ticket.setStatus(Status.ASSIGNED);
            }
        }

        Ticket saved = ticketRepository.save(ticket);

        AuditLog log = auditLogRepository.save(
                new AuditLog(
                        saved,
                        user,
                        action,
                        oldValue,
                        saved.getStatus().name()
                )
        );

        TicketResponse response = mapTicket(saved);

        response.setAuditLogs(List.of(new AuditLogResponse(
                log.getId(),
                log.getUpdatedBy().getUsername(),
                log.getAction(),
                log.getTimestamp()
        )));

        return response;
    }

    // ================= MAPPERS =================

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