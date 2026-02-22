package com.beginner_project.ticket_system.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    public TicketCreateRequest() {}

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
}
