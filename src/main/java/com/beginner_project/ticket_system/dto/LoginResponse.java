package com.beginner_project.ticket_system.dto;

public class LoginResponse {

    private String access;
    private String refresh;
    private long expiryTime;

    public LoginResponse(String access, String refresh, long expiryTime) {
        this.access = access;
        this.refresh = refresh;
        this.expiryTime = expiryTime;
    }

    public String getAccess() {
        return access;
    }

    public String getRefresh() {
        return refresh;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}
