package com.beginner_project.ticket_system.dto;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String updatedBy;
    private String action;
    private LocalDateTime timestamp;

    public AuditLogResponse(Long id, String updatedBy, String action, LocalDateTime timestamp) {
        this.id = id;
        this.updatedBy = updatedBy;
        this.action = action;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
