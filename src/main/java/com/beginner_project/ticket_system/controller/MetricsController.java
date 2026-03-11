package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.MetricsResponse;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.service.MetricsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics(
            @RequestParam(defaultValue = "24h") String period
    ) {
        Users user = getCurrentUser();
        if (user.getRole() != Role.ADMIN)
            throw new BusinessException("Admin only", HttpStatus.FORBIDDEN);

        return ResponseEntity.ok(metricsService.getMetrics(period));
    }

    private Users getCurrentUser() {
        return (Users) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}