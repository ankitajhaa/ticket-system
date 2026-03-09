package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.entity.NotificationLog;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.enums.NotificationStatus;
import com.beginner_project.ticket_system.enums.NotificationType;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final EmailService emailService;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${notification.retry.max-attempts:3}")
    private int maxRetries;

    public NotificationServiceImpl(
            EmailService emailService,
            NotificationLogRepository notificationLogRepository
    ) {
        this.emailService = emailService;
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override

    public void sendNotification(Ticket ticket, String recipientEmail,
                                  NotificationType type, String subject, String body) {

        // skippinggg app ;level , just select query so fast handles most cases
        //db level check jo hai handles race conditions not catched by this and throws exception
        boolean alreadySent = notificationLogRepository
                .existsByTicketAndRecipientEmailAndNotificationTypeAndStatusAndInvalidatedFalse(
                        ticket, recipientEmail, type, NotificationStatus.SENT);

        if (alreadySent) {
            logger.info("Notification already sent for ticket {} type {} to {} — skipping",
                    ticket.getId(), type, recipientEmail);
            return;
        }

        //saving as pending so mid crash still has it and can run again 
        NotificationLog log = new NotificationLog();
        log.setTicket(ticket);
        log.setRecipientEmail(recipientEmail);
        log.setNotificationType(type);
        log.setStatus(NotificationStatus.PENDING);
        log.setRetryCount(0);
        log.setLastAttemptAt(LocalDateTime.now());
        log.setSubject(subject);
        log.setBody(body);

        try{
           notificationLogRepository.save(log);
        } catch(Exception e)
        {
            logger.warn("Duplicate notification blocked by DB constraint for ticket id {} of type {} sent to {}", 
                    ticket.getId(), type, recipientEmail);
                    return;
        }      
        attemptSend(log, subject, body);
    }

    @Override
    public void retryNotification(Long notificationId) {

        NotificationLog log = notificationLogRepository.findById(notificationId)
                .orElseThrow(() ->new BusinessException("Notification not found", HttpStatus.NOT_FOUND));
        if (log.getStatus() != NotificationStatus.FAILED)
        {
            throw new BusinessException( "Only FAILED notifications can be retriggered", HttpStatus.CONFLICT);
        }
        attemptSend(log, log.getSubject(), log.getBody());
    }
     @Async
     void attemptSend(NotificationLog log, String subject, String body) {
        try {
            emailService.sendEmail(log.getRecipientEmail(), subject, body);
            log.setStatus(NotificationStatus.SENT);
            log.setLastAttemptAt(LocalDateTime.now());
            notificationLogRepository.save(log);
            logger.info("Notification sent to {} type {}", 
                    log.getRecipientEmail(), log.getNotificationType());
        } catch (Exception e) {
            log.setStatus(NotificationStatus.FAILED);
            log.setRetryCount(log.getRetryCount() + 1);
            log.setLastAttemptAt(LocalDateTime.now());
            notificationLogRepository.save(log);
            logger.error("Notification failed for ticket {} to {}: {}",
                    log.getTicket().getId(), log.getRecipientEmail(), e.getMessage());
        }
    }
}