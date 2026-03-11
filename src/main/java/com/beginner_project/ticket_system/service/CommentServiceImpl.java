package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.CommentRequest;
import com.beginner_project.ticket_system.dto.CommentResponse;
import com.beginner_project.ticket_system.entity.AuditLog;
import com.beginner_project.ticket_system.entity.Comment;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.ActorType;
import com.beginner_project.ticket_system.enums.AuditAction;
import com.beginner_project.ticket_system.enums.CommentType;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.repository.AuditLogRepository;
import com.beginner_project.ticket_system.repository.CommentRepository;
import com.beginner_project.ticket_system.repository.TicketRepository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final AuditLogRepository auditLogRepository;

    public CommentServiceImpl(CommentRepository commentRepository,TicketRepository ticketRepository, AuditLogRepository auditLogRepository)
    {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public CommentResponse addComment(Long ticketId, CommentRequest request, Users user) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->  new BusinessException("Ticket not found", HttpStatus.NOT_FOUND));
        if (ticket.getStatus() == Status.CLOSED)
            throw new BusinessException("Closed tickets cannot receive new comments", HttpStatus.CONFLICT);

        if (user.getRole() == Role.CUSTOMER) 
        {
            if (!ticket.getCreatedBy().getId().equals(user.getId()))
                throw new BusinessException( "You can only comment on your own tickets", HttpStatus.FORBIDDEN);
            if (request.getCommentType() == CommentType.INTERNAL)
                throw new BusinessException(   "Customers cannot post internal comments", HttpStatus.FORBIDDEN);
        }

        if (user.getRole() == Role.SUPPORT_AGENT)
            {
            boolean isAssigned = ticket.getAssignedAgent() != null &&
                    ticket.getAssignedAgent().getId().equals(user.getId());
            if (!isAssigned)
                throw new BusinessException( "You can only comment on your assigned tickets", HttpStatus.FORBIDDEN);
        }

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(user);
        comment.setContent(request.getContent());
        comment.setCommentType(request.getCommentType());
        comment.setCreatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        ActorType actorType;

if (user.getRole() == Role.ADMIN)
    actorType = ActorType.ADMIN;
else if (user.getRole() == Role.SUPPORT_AGENT)
    actorType = ActorType.AGENT;
else
    actorType = ActorType.CUSTOMER;

auditLogRepository.save(
        new AuditLog(
                ticket,
                user,
                actorType,
                AuditAction.COMMENT_ADDED,
                "",
                saved.getContent()
        )
);
        return mapComment(saved);
    }

    @Override
    public Page<CommentResponse> getComments(Long ticketId, int page, int size, Users user)
    {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found", HttpStatus.NOT_FOUND));
        if (user.getRole() == Role.CUSTOMER) {
            if (!ticket.getCreatedBy().getId().equals(user.getId()))
  throw new BusinessException("You do not have permission to view these comments", HttpStatus.FORBIDDEN);
        }

        if (user.getRole() == Role.SUPPORT_AGENT)
        {
            boolean isAssigned = ticket.getAssignedAgent() != null &&
                    ticket.getAssignedAgent().getId().equals(user.getId());
            if (!isAssigned)
                throw new BusinessException("You can only view comments on your assigned tickets", HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        if (user.getRole() == Role.CUSTOMER)
        {
            return commentRepository
                    .findByTicketAndCommentType(ticket, CommentType.PUBLIC, pageable)
                    .map(this::mapComment);
        }

        return commentRepository
                .findByTicket(ticket, pageable)
                .map(this::mapComment);
    }

    private CommentResponse mapComment(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getUsername(),
                comment.getContent(),
                comment.getCommentType().name(),
                comment.getCreatedAt()
        );
    }
}