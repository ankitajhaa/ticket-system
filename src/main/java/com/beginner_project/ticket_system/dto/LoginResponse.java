package com.beginner_project.ticket_system.dto;

public class LoginResponse {

    private String access;
    private String refresh;
    private long expiry_time;

    public LoginResponse(String access, String refresh, long expiry_time) {
        this.access = access;
        this.refresh = refresh;
        this.expiry_time = expiry_time;
    }

    public String getAccess() {
        return access;
    }

    public String getRefresh() {
        return refresh;
    }

    public long getExpiry_time() {
        return expiry_time;
    }
}
