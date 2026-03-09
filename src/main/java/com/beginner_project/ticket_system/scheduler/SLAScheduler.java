package com.beginner_project.ticket_system.scheduler;

import com.beginner_project.ticket_system.entity.SLAConfig;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.enums.NotificationType;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.repository.SLAConfigRepository;
import com.beginner_project.ticket_system.repository.TicketRepository;
import com.beginner_project.ticket_system.service.NotificationService;
import com.beginner_project.ticket_system.util.NotificationTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SLAScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SLAScheduler.class);

    private final TicketRepository ticketRepository;
    private final SLAConfigRepository slaConfigRepository;
    private final NotificationService notificationService;

    private static final List<Status> EXCLUDED_STATUSES = List.of(
            Status.RESOLVED,
            Status.CLOSED
    );

    public SLAScheduler(
            TicketRepository ticketRepository,
            SLAConfigRepository slaConfigRepository,
            NotificationService notificationService
    ) {
        this.ticketRepository = ticketRepository;
        this.slaConfigRepository = slaConfigRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRateString = "${sla.scheduler.interval:1800000}")
    @Transactional
    public void checkSLABreaches() {

        logger.info("SLA breach check started at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        // ── PASS 1 — BREACH DETECTION ─────────────────────────────────
        List<Ticket> breachedTickets = ticketRepository
                .findTicketsBreachedAndNotNotified(now, EXCLUDED_STATUSES);

        for (Ticket ticket : breachedTickets) {

            ticket.setSlaBreached(true);
            ticketRepository.save(ticket);
            logger.info("Ticket {} marked as SLA breached", ticket.getId());

            // notify assigned agent
            if (ticket.getAssignedAgent() != null) {
                notificationService.sendNotification(
                        ticket,
                        ticket.getAssignedAgent().getEmail(),
                        NotificationType.SLA_BREACH,
                        NotificationTemplates.slaBreachSubject(ticket),
                        NotificationTemplates.slaBreachBody(ticket,
                                ticket.getAssignedAgent().getUsername())
                );
            }

            // always notify admin
            notificationService.sendNotification(
                    ticket,
                    "kumar.aryan.1303+admin@gmail.com",
                    NotificationType.SLA_BREACH,
                    NotificationTemplates.slaBreachSubject(ticket),
                    NotificationTemplates.slaBreachBody(ticket, "Admin")
            );
        }

        // ── PASS 2 — REMINDER DETECTION ───────────────────────────────
        List<Ticket> activeTickets = ticketRepository
                .findActiveTicketsWithUpcomingDeadline(now, EXCLUDED_STATUSES);

        for (Ticket ticket : activeTickets) {

            SLAConfig config = slaConfigRepository.findByPriority(ticket.getPriority());
            if (config == null) continue;

            long minutesUntilDeadline = Duration.between(now, ticket.getSlaDeadline()).toMinutes();
            String[] reminders = config.getReminderHours().split(",");

            for (String reminder : reminders) {
                double reminderHours = Double.parseDouble(reminder.trim());
                long reminderMinutes = (long)(reminderHours * 60);

                if (minutesUntilDeadline <= reminderMinutes
                        && minutesUntilDeadline > reminderMinutes - 30) {

                    logger.info("Ticket {} SLA reminder — {} hours until deadline",
                            ticket.getId(), reminderHours);

                    // notify assigned agent only
                    if (ticket.getAssignedAgent() != null) {
                        notificationService.sendNotification(
                                ticket,
                                ticket.getAssignedAgent().getEmail(),
                                NotificationType.SLA_WARNING,
                                NotificationTemplates.slaWarningSubject(ticket),
                                NotificationTemplates.slaWarningBody(ticket, reminderHours)
                        );
                    }
                }
            }
        }

        logger.info("SLA breach check completed. Breached: {}", breachedTickets.size());
    }
}