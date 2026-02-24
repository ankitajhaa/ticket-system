package com.beginner_project.ticket_system.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private String status;

    private UserResponse createdBy;
    private UserResponse assignedAgent;

    private LocalDateTime createdAt;

    private List<AuditLogResponse> auditLogs;

    public TicketResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserResponse getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserResponse createdBy) {
        this.createdBy = createdBy;
    }

    public UserResponse getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(UserResponse assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AuditLogResponse> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLogResponse> auditLogs) {
        this.auditLogs = auditLogs;
    }
}