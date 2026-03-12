package com.beginner_project.ticket_system.dto;

import java.time.LocalDateTime;

public class NotificationLogResponse {
       private Long id;
    private Long ticketId;
    private String recipientEmail;
    private String notificationType;
    private String status;
    private Integer retryCount;
    private LocalDateTime lastAttemptAt;
    public NotificationLogResponse(Long id, Long ticketId, String recipientEmail, String notificationType,
            String status, Integer retryCount, LocalDateTime lastAttemptAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.recipientEmail = recipientEmail;
        this.notificationType = notificationType;
        this.status = status;
        this.retryCount = retryCount;
        this.lastAttemptAt = lastAttemptAt;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTicketId() {
        return ticketId;
    }
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }
    public String getRecipientEmail() {
        return recipientEmail;
    }
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    public String getNotificationType() {
        return notificationType;
    }
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Integer getRetryCount() {
        return retryCount;
    }
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }
    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

}
