package com.beginner_project.ticket_system.dto;

import java.time.LocalDateTime;

public record MetricsResponse(
        String period,
        LocalDateTime from,
        LocalDateTime to,
        long totalCreated,
        long totalResolved,
        long totalSlaBreached,
        long totalActive,
        double avgResolutionSeconds
) {}