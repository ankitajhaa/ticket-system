package com.beginner_project.ticket_system.entity;

import com.beginner_project.ticket_system.enums.Action;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "Audit_log")
@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", nullable = false)
    private Users updatedBy;

    @Column(name = "action_type", nullable = false)
    private String action;

    @Lob
    @Column(name = "old_value", columnDefinition = "TEXT", nullable = false)
    private String oldValue;

    @Lob
    @Column(name = "new_value", columnDefinition = "TEXT", nullable = false)
    private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(Ticket ticket, Users updatedBy, Action action, String oldValue, String newValue) {
        this.ticket = ticket;
        this.updatedBy = updatedBy;
        this.action = action.name();
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicketId() {
        return ticket;
    }

    public void setTicketId(Ticket ticket) {
        this.ticket = ticket;
    }

    public Users getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Users updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}