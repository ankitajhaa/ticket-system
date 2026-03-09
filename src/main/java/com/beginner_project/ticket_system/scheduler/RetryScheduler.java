package com.beginner_project.ticket_system.scheduler;

import com.beginner_project.ticket_system.entity.NotificationLog;
import com.beginner_project.ticket_system.enums.NotificationStatus;
import com.beginner_project.ticket_system.repository.NotificationLogRepository;
import com.beginner_project.ticket_system.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RetryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RetryScheduler.class);

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;

    @Value("${notification.retry.max-attempts:3}")
    private int maxRetries;

    public RetryScheduler(
            NotificationLogRepository notificationLogRepository,
            EmailService emailService
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRateString = "${notification.retry.interval:300000}")
    public void retryFailedNotifications() {

        logger.info("Retry scheduler started at {}", LocalDateTime.now());

        List<NotificationLog> failedLogs = notificationLogRepository
                .findByStatusInAndRetryCountLessThan(List.of(NotificationStatus.FAILED,NotificationStatus.PENDING), maxRetries);

        for (NotificationLog log : failedLogs) {
            try {
             /*   String subject = "Retry: " + log.getNotificationType().name()
                        + " for Ticket #" + log.getTicket().getId();
                String body = "This is a retry notification for ticket #"
                        + log.getTicket().getId()
                        + "\nType: " + log.getNotificationType().name();*/

                emailService.sendEmail(log.getRecipientEmail(), log.getSubject(),log.getBody());

                log.setStatus(NotificationStatus.SENT);
                log.setLastAttemptAt(LocalDateTime.now());
                notificationLogRepository.save(log);

                logger.info("Retry successful for notification {}", log.getId());

            } catch (Exception e) {
                log.setRetryCount(log.getRetryCount() + 1);
                log.setLastAttemptAt(LocalDateTime.now());

                if (log.getRetryCount() >= maxRetries) {
                    logger.warn("Max retries reached for notification {}", log.getId());
                }

                notificationLogRepository.save(log);
                logger.error("Retry failed for notification {}: {}", log.getId(), e.getMessage());
            }
        }

        logger.info("Retry scheduler completed. Processed: {}", failedLogs.size());
    }
}