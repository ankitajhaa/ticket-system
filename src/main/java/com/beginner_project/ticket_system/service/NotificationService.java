package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.NotificationLogResponse;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.enums.NotificationType;

public interface NotificationService {
    void sendNotification(Ticket ticket, String recipientEmail, 
                           NotificationType type, String subject, String body);
    NotificationLogResponse retryNotification(Long notificationId);
}