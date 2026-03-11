package com.beginner_project.ticket_system.entity;

import com.beginner_project.ticket_system.enums.ActorType;
import com.beginner_project.ticket_system.enums.AuditAction;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Users updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    private ActorType actorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AuditAction action;

   @Column(name = "old_value", columnDefinition = "TEXT", nullable = false)
private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT", nullable = false)
private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(Ticket ticket, Users updatedBy, ActorType actorType,
                    AuditAction action, String oldValue, String newValue) {
        this.ticket = ticket;
        this.updatedBy = updatedBy;
        this.actorType = actorType;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public Users getUpdatedBy() {
        return updatedBy;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public void setUpdatedBy(Users updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setActorType(ActorType actorType) {
        this.actorType = actorType;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}