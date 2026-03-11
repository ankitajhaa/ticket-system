package com.beginner_project.ticket_system.metrics;

import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.repository.TicketRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class TicketMetrics {

    private final Counter ticketsCreatedCounter;
    private final Counter ticketsResolvedCounter;
    private final Counter slaBreachedCounter;
    private final Timer resolutionTimer;
    private final MeterRegistry meterRegistry;

    public TicketMetrics(MeterRegistry meterRegistry, TicketRepository ticketRepository) {
        this.meterRegistry = meterRegistry;

        this.ticketsCreatedCounter = Counter.builder("tickets_new")
                .description("Total tickets created")
                .register(meterRegistry);

        this.ticketsResolvedCounter = Counter.builder("tickets_resolved")
                .description("Total tickets resolved")
                .register(meterRegistry);

        this.slaBreachedCounter = Counter.builder("tickets_sla_breached")
                .description("Total SLA breaches")
                .register(meterRegistry);

        this.resolutionTimer = Timer.builder("ticket_resolution_time")
                .description("Ticket resolution time in seconds")
                .register(meterRegistry);

        Gauge.builder("tickets_active_open_total", ticketRepository, repo ->
                        repo.countByStatusNotIn(List.of(Status.RESOLVED, Status.CLOSED)))
                .description("Total active open tickets")
                .register(meterRegistry);
    }

    public void incrementTicketsCreated() {
        ticketsCreatedCounter.increment();
    }

    public void incrementTicketsResolved() {
        ticketsResolvedCounter.increment();
    }

    public void incrementSlaBreached() {
        slaBreachedCounter.increment();
    }

    public void recordResolutionTime(long seconds) {
        resolutionTimer.record(seconds, TimeUnit.SECONDS);
    }

    public Timer getResolutionTimer() {
        return resolutionTimer;
    }
}