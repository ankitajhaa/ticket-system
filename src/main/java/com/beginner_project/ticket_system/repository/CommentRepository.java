package com.beginner_project.ticket_system.repository;

import com.beginner_project.ticket_system.entity.Comment;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.enums.CommentType;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTicket(Ticket ticket, Pageable pageable);
    Page<Comment> findByTicketAndCommentType(Ticket ticket, CommentType commentType, Pageable pageable);
    List<Comment> findByTicket(Ticket ticket);
    List<Comment> findByTicketAndCommentType(Ticket ticket, CommentType commentType);
}