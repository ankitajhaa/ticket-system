package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.NotificationLogResponse;
import com.beginner_project.ticket_system.entity.NotificationLog;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.repository.NotificationLogRepository;
import com.beginner_project.ticket_system.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationController(
            NotificationService notificationService,
            NotificationLogRepository notificationLogRepository
    ) {
        this.notificationService = notificationService;
        this.notificationLogRepository = notificationLogRepository;
    }

    @PostMapping("/{notificationId}/retry")
public ResponseEntity<NotificationLogResponse> retryNotification(
        @PathVariable("notificationId") Long notificationId) {

    Users user = (Users) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

    if (user.getRole() != Role.ADMIN) {
        throw new BusinessException("Admin only", HttpStatus.FORBIDDEN);
    }

    NotificationLogResponse response = notificationService.retryNotification(notificationId);
    return ResponseEntity.ok(response);
}
}