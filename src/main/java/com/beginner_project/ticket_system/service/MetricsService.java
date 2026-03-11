package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.MetricsResponse;

public interface MetricsService {
    MetricsResponse getMetrics(String period);
}