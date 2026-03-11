package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.MetricsResponse;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.metrics.TicketMetrics;
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
    private final TicketMetrics ticketMetrics;

    public MetricsServiceImpl(MeterRegistry meterRegistry,
                               TicketRepository ticketRepository,
                               TicketMetrics ticketMetrics) {
        this.meterRegistry = meterRegistry;
        this.ticketRepository = ticketRepository;
        this.ticketMetrics = ticketMetrics;
    }

    @Override
    public MetricsResponse getMetrics(String period) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = parsePeriod(period, to);

        double totalCreated = meterRegistry.counter("tickets_new").count();
        double totalResolved = meterRegistry.counter("tickets_resolved").count();
        double totalBreaches = meterRegistry.counter("tickets_sla_breached").count();

        long totalActive = ticketRepository.countByStatusNotIn(
                List.of(Status.RESOLVED, Status.CLOSED));

        double avgResolutionSeconds = ticketMetrics.getResolutionTimer()
                .mean(TimeUnit.SECONDS);

        return new MetricsResponse(
                period,
                from,
                to,
                (long) totalCreated,
                (long) totalResolved,
                (long) totalBreaches,
                totalActive,
                Math.round(avgResolutionSeconds * 100.0) / 100.0
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