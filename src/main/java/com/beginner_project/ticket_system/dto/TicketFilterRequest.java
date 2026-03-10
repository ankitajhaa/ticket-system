package com.beginner_project.ticket_system.dto;

import com.beginner_project.ticket_system.enums.Priority;
import com.beginner_project.ticket_system.enums.Status;

import java.time.LocalDateTime;

public class TicketFilterRequest {

    private Status status;
    private Priority priority;
    private Long assignedAgentId;
    private Long customerId;
    private Boolean slaBreached;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime deadlineFrom;
    private LocalDateTime deadlineTo;
    
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public Priority getPriority() {
        return priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public Long getAssignedAgentId() {
        return assignedAgentId;
    }
    public void setAssignedAgentId(Long assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public Boolean getSlaBreached() {
        return slaBreached;
    }
    public void setSlaBreached(Boolean slaBreached) {
        this.slaBreached = slaBreached;
    }
    public LocalDateTime getCreatedFrom() {
        return createdFrom;
    }
    public void setCreatedFrom(LocalDateTime createdFrom) {
        this.createdFrom = createdFrom;
    }
    public LocalDateTime getCreatedTo() {
        return createdTo;
    }
    public void setCreatedTo(LocalDateTime createdTo) {
        this.createdTo = createdTo;
    }
    public LocalDateTime getDeadlineFrom() {
        return deadlineFrom;
    }
    public void setDeadlineFrom(LocalDateTime deadlineFrom) {
        this.deadlineFrom = deadlineFrom;
    }
    public LocalDateTime getDeadlineTo() {
        return deadlineTo;
    }
    public void setDeadlineTo(LocalDateTime deadlineTo) {
        this.deadlineTo = deadlineTo;
    }
}