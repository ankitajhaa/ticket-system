package com.beginner_project.ticket_system.scheduler;

import com.beginner_project.ticket_system.entity.SLAConfig;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.enums.Status;
import com.beginner_project.ticket_system.repository.SLAConfigRepository;
import com.beginner_project.ticket_system.repository.TicketRepository;
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

    private static final List<Status>EXCLUDED_STATUSES = List.of(Status.RESOLVED,Status.CLOSED);

    public SLAScheduler( TicketRepository ticketRepository, SLAConfigRepository slaConfigRepository  ) 
    {
        this.ticketRepository = ticketRepository;
        this.slaConfigRepository = slaConfigRepository;
    }

    @Scheduled(fixedRateString = "${sla.scheduler.interval:1800000}")
    @Transactional
    public void checkSLABreaches() {

        logger.info("SLA breach check started at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        List<Ticket> breachedTickets = ticketRepository.findTicketsBreachedAndNotNotified(now, EXCLUDED_STATUSES);

        for (Ticket ticket : breachedTickets) {
            ticket.setSlaBreached(true);
            ticketRepository.save(ticket);
            logger.info("Ticket {} marked as SLA breached", ticket.getId());
           
            //we have to hit notification here in next step
        }

        List<Ticket> activeTickets = ticketRepository
                .findActiveTicketsWithUpcomingDeadline(now, EXCLUDED_STATUSES);

        for (Ticket ticket : activeTickets)
            {
            SLAConfig config = slaConfigRepository.findByPriority(ticket.getPriority());
            if (config == null) continue;
            long minutesUntilDeadline = Duration.between(now, ticket.getSlaDeadline()).toMinutes();

            String[] reminders = config.getReminderHours().split(",");

            for (String reminder : reminders)
                {
                double reminderHours = Double.parseDouble(reminder.trim());
                long reminderMinutes = (long)(reminderHours * 60);

        
         if (minutesUntilDeadline <= reminderMinutes && minutesUntilDeadline > reminderMinutes - 30)
            {
             logger.info("Ticket x{} SLA reminder—{}hours until deadline",ticket.getId(), reminderHours);
              //lets do the warnings here later
            }
            }
        }
        logger.info("SLA breach check completed. Breached: {}", breachedTickets.size());
    }
}