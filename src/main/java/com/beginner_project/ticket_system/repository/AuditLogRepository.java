package com.beginner_project.ticket_system.repository;

import com.beginner_project.ticket_system.entity.AuditLog;
import com.beginner_project.ticket_system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTicket(Ticket ticket);
}