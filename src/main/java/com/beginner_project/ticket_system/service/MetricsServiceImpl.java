package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.MetricsResponse;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.repository.TicketRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final MeterRegistry meterRegistry;
    private final TicketRepository ticketRepository;

    public MetricsServiceImpl(MeterRegistry meterRegistry,
                               TicketRepository ticketRepository) {
        this.meterRegistry = meterRegistry;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public MetricsResponse getMetrics(String period) {

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = parsePeriod(period, to);

        // read from Micrometer counters — no DB queries
        double totalCreated = meterRegistry.counter("tickets_created_total").count();
        double totalResolved = meterRegistry.counter("tickets_resolved_total").count();
        double totalBreaches = meterRegistry.counter("tickets_sla_breached_total").count();

        // active open tickets — gauge, one DB query
        long totalActive = ticketRepository.countByStatusNotIn(
                List.of(Status.RESOLVED, Status.CLOSED));

        // average resolution time from Timer
        double avgResolutionHours = meterRegistry
                .timer("ticket_resolution_time")
                .mean(TimeUnit.HOURS);

        return new MetricsResponse(
                period, from, to,
                (long) totalCreated,
                (long) totalResolved,
                (long) totalBreaches,
                totalActive,
                Math.round(avgResolutionHours * 100.0) / 100.0
        );
    }

    private LocalDateTime parsePeriod(String period, LocalDateTime to) {
        if (period == null || period.isBlank())
            throw new BusinessException("Period is required", HttpStatus.BAD_REQUEST);

        String lower = period.toLowerCase().trim();

        try {
            if (lower.endsWith("h")) {
                int hours = Integer.parseInt(lower.replace("h", ""));
                return to.minusHours(hours);
            } else if (lower.endsWith("d")) {
                int days = Integer.parseInt(lower.replace("d", ""));
                return to.minusDays(days);
            } else if (lower.endsWith("w")) {
                int weeks = Integer.parseInt(lower.replace("w", ""));
                return to.minusWeeks(weeks);
            } else {
                throw new BusinessException(
                        "Invalid period format. Use: 1h, 24h, 3d, 7d, 1w",
                        HttpStatus.BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(
                    "Invalid period format. Use: 1h, 24h, 3d, 7d, 1w",
                    HttpStatus.BAD_REQUEST);
        }
    }
}