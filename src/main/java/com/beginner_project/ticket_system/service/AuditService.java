package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.ActorType;
import com.beginner_project.ticket_system.enums.AuditAction;

public interface AuditService {

    void logAction(
            Ticket ticket,
            Users user,
            ActorType actorType,
            AuditAction action,
            String oldValue,
            String newValue
    );
}