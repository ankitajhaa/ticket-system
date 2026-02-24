package com.beginner_project.ticket_system.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketUpdateRequest {

    @NotBlank
    private String action;

    private String status;
    private Long assignedAgent;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(Long assignedAgent) {
        this.assignedAgent = assignedAgent;
    }
}