package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.enums.Action;
import com.beginner_project.ticket_system.enums.CommentType;
import com.beginner_project.ticket_system.enums.NotificationType;
import com.beginner_project.ticket_system.enums.Priority;
import com.beginner_project.ticket_system.repository.AuditLogRepository;
import com.beginner_project.ticket_system.repository.CommentRepository;
import com.beginner_project.ticket_system.repository.NotificationLogRepository;
import com.beginner_project.ticket_system.repository.SLAConfigRepository;
import com.beginner_project.ticket_system.repository.TicketRepository;
import com.beginner_project.ticket_system.repository.UserRepository;
import com.beginner_project.ticket_system.specification.TicketSpecification;
import com.beginner_project.ticket_system.util.NotificationTemplates;
import com.beginner_project.ticket_system.dto.*;
import com.beginner_project.ticket_system.entity.AuditLog;
import com.beginner_project.ticket_system.entity.Comment;
import com.beginner_project.ticket_system.entity.SLAConfig;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.exception.BusinessException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final SLAConfigRepository slaConfigRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationService notificationService;
    private final CommentRepository commentRepository;

    public TicketServiceImpl(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            SLAConfigRepository slaConfigRepository,
            NotificationService notificationService,
            NotificationLogRepository notificationLogRepository,
            CommentRepository commentRepository)
    {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.slaConfigRepository = slaConfigRepository;
        this.notificationService = notificationService;
        this.notificationLogRepository = notificationLogRepository;
        this.commentRepository=commentRepository;
    }

    // ================= CREATE =================

    @Override
    
    public TicketResponse createTicket(TicketCreateRequest request, Users user) {

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Admins cannot create tickets", HttpStatus.FORBIDDEN);
        }

        logger.info("Creating ticket by user: {}", user.getUsername());

        Priority priority = request.getPriority();
        if (priority == null) {
            priority = Priority.MEDIUM;
        }

        SLAConfig slaConfig = slaConfigRepository.findByPriority(priority);
        if (slaConfig == null) {
            throw new BusinessException("SLA Config not found for priority" + priority, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LocalDateTime slaDeadline = LocalDateTime.now().plusHours(slaConfig.getResolutionHours());

        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                Status.OPEN,
                priority,
                slaDeadline,
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

        List<Comment>comments;

        if(user.getRole()==Role.CUSTOMER)
        {
            comments=commentRepository.findByTicketAndCommentType(ticket,CommentType.PUBLIC);
        }
        else
        {
            comments=commentRepository.findByTicket(ticket);
        }

        response.setComments(comments.stream().map(c->new CommentResponse(c.getId(),c.getAuthor().getUsername(),c.getContent(),c.getCommentType().name(),c.getCreatedAt())).toList());
        return response;
    }

    // ================= UPDATE =================

    @Override
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

                notificationLogRepository.invalidateByTicketAndNotificationTypeIn(
            ticket,
            List.of(NotificationType.SLA_BREACH, NotificationType.SLA_WARNING,
                    NotificationType.TICKET_ASSIGNED)
    );

                ticket.setAssignedAgent(user);
                ticket.setStatus(Status.ASSIGNED);
            }

            case UPDATE_PROGRESS -> {
                boolean isAssignedAgent =
                        ticket.getAssignedAgent() != null &&
                                ticket.getAssignedAgent().getId().equals(user.getId());
                if (user.getRole() != Role.ADMIN &&
                        !(user.getRole() == Role.SUPPORT_AGENT && isAssignedAgent))
                    throw new BusinessException("You are not allowed", HttpStatus.FORBIDDEN);
                if (request.getStatus() == null)
                    throw new BusinessException("Status is required for UPDATE_PROGRESS", HttpStatus.BAD_REQUEST);
                Status newStatus;
                try {
                    newStatus = Status.valueOf(request.getStatus().toUpperCase());
                } catch (Exception e) {
                    throw new BusinessException("Invalid status value", HttpStatus.BAD_REQUEST);
                }
                if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
        notificationLogRepository.invalidateByTicketAndNotificationTypeIn(
                ticket,
                List.of(NotificationType.SLA_BREACH, NotificationType.SLA_WARNING)
        );
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


                 notificationLogRepository.invalidateByTicketAndNotificationTypeIn(
            ticket,
            List.of(NotificationType.SLA_BREACH, NotificationType.SLA_WARNING,
                    NotificationType.TICKET_ASSIGNED)
    );                
                ticket.setAssignedAgent(agent);
                ticket.setStatus(Status.ASSIGNED);
            }

            case SET_PRIORITY -> {
                if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPPORT_AGENT)
                    throw new BusinessException("You are not allowed to perform this action", HttpStatus.FORBIDDEN);
                if (request.getPriority() == null)
                    throw new BusinessException("Priority is required for SET_PRIORITY", HttpStatus.BAD_REQUEST);
                Priority newPriority = request.getPriority();
                if (ticket.getPriority() == newPriority)
                    throw new BusinessException("Ticket already has this priority", HttpStatus.CONFLICT);
                if (ticket.getStatus() == Status.RESOLVED || ticket.getStatus() == Status.CLOSED)
                    throw new BusinessException("Cannot change priority on resolved or closed tickets", HttpStatus.CONFLICT);
                SLAConfig slaConfig = slaConfigRepository.findByPriority(newPriority);
                if (slaConfig == null)
                    throw new BusinessException("SLA config not found for priority: " + newPriority, HttpStatus.INTERNAL_SERVER_ERROR);
                oldValue = "priority=" + ticket.getPriority() + ",slaDeadline=" + ticket.getSlaDeadline();
                LocalDateTime newDeadline = LocalDateTime.now().plusHours(slaConfig.getResolutionHours());


                 notificationLogRepository.invalidateByTicketAndNotificationTypeIn(
            ticket,
            List.of(NotificationType.SLA_BREACH, NotificationType.SLA_WARNING)
    );

                ticket.setPriority(newPriority);
                ticket.setSlaDeadline(newDeadline);
                ticket.setSlaBreached(false);
            }
        }

        // ── single save for all actions ──────────────────────────────
        Ticket saved = ticketRepository.save(ticket);

        // ── single audit log for all actions ─────────────────────────
        String newValue = (action == Action.SET_PRIORITY)
                ? "priority=" + saved.getPriority() + ",slaDeadline=" + saved.getSlaDeadline()
                : saved.getStatus().name();

        AuditLog log = auditLogRepository.save(
                new AuditLog(saved, user, action, oldValue, newValue)
        );

        // ── notifications after save ──────────────────────────────────
        if (action == Action.CLAIM || action == Action.ASSIGN) {
            notificationService.sendNotification(
                    saved,
                    saved.getCreatedBy().getEmail(),
                    NotificationType.TICKET_ASSIGNED,
                    NotificationTemplates.ticketAssignedCustomerSubject(saved),
                    NotificationTemplates.ticketAssignedCustomerBody(saved)
            );
            notificationService.sendNotification(
                    saved,
                    saved.getAssignedAgent().getEmail(),
                    NotificationType.TICKET_ASSIGNED,
                    NotificationTemplates.ticketAssignedAgentSubject(saved),
                    NotificationTemplates.ticketAssignedAgentBody(saved)
            );
        }

        if (action == Action.SET_PRIORITY && saved.getAssignedAgent() != null) {
            String oldPriority = oldValue.split(",")[0].replace("priority=", "");
            String oldDeadline = oldValue.split(",")[1].replace("slaDeadline=", "");
            notificationService.sendNotification(
                    saved,
                    saved.getAssignedAgent().getEmail(),
                    NotificationType.PRIORITY_CHANGED,
                    NotificationTemplates.priorityChangedSubject(saved),
                    NotificationTemplates.priorityChangedBody(saved, oldPriority, oldDeadline)
            );
        }

        // ── single response for all actions ──────────────────────────
        TicketResponse response = mapTicket(saved);
        response.setAuditLogs(List.of(new AuditLogResponse(
                log.getId(),
                log.getUpdatedBy().getUsername(),
                log.getAction(),
                log.getTimestamp()
        )));
        return response;
    }


    @Override
public Page<TicketResponse> searchTickets(
        TicketFilterRequest filter,
        int page,
        int size,
        String sortBy,
        String sortDir,
        Users user
) {
    // build sort direction
    Sort.Direction direction = sortDir.equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;

    // build pageable — page number, size, sort field and direction
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    // build specification with filters + role based access
    TicketSpecification spec = new TicketSpecification(filter, user);

    // execute query — returns Page<Ticket>
    Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);

    // map to Page<TicketResponse>
    return tickets.map(this::mapTicket);
}

    // ================= MAPPERS =================

    private TicketResponse mapTicket(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus().name());
        response.setPriority(ticket.getPriority() != null ? ticket.getPriority().name() : null);
        response.setSlaDeadline(ticket.getSlaDeadline());
        response.setSlaBreached(ticket.getSlaBreached());
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