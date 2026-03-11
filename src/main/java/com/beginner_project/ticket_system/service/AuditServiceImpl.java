package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.entity.AuditLog;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.ActorType;
import com.beginner_project.ticket_system.enums.AuditAction;
import com.beginner_project.ticket_system.repository.AuditLogRepository;
import com.beginner_project.ticket_system.service.AuditService;

import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void logAction(
            Ticket ticket,
            Users user,
            ActorType actorType,
            AuditAction action,
            String oldValue,
            String newValue
    ) {

        AuditLog log = new AuditLog();

        log.setTicket(ticket);
        log.setUpdatedBy(user);
        log.setActorType(actorType);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);

        auditLogRepository.save(log);
    }
}