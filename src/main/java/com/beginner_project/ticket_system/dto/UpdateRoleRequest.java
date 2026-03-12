package com.beginner_project.ticket_system.dto;

import com.beginner_project.ticket_system.enums.Role;

import jakarta.validation.constraints.NotNull;

public class UpdateRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}