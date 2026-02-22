package com.beginner_project.ticket_system.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private Users updatedBy;

    @Column(name = "action_type", nullable = false)
    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT", nullable = false)
    private String old_value;

    @Column(name = "new_value", columnDefinition = "TEXT", nullable = false)
    private String new_value;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(Ticket ticket_id, Users updated_by, String action, String old_value, String new_value) {
        this.ticket = ticket_id;
        this.updatedBy = updated_by;
        this.action = action;
        this.old_value = old_value;
        this.new_value = new_value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket_id() {
        return ticket;
    }

    public void setTicket_id(Ticket ticket_id) {
        this.ticket = ticket_id;
    }

    public Users getUpdated_by() {
        return updatedBy;
    }

    public void setUpdated_by(Users updated_by) {
        this.updatedBy = updated_by;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOld_value() {
        return old_value;
    }

    public void setOld_value(String old_value) {
        this.old_value = old_value;
    }

    public String getNew_value() {
        return new_value;
    }

    public void setNew_value(String new_value) {
        this.new_value = new_value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
