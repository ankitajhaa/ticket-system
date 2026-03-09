package com.beginner_project.ticket_system.repository;

import com.beginner_project.ticket_system.entity.NotificationLog;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.enums.NotificationStatus;
import com.beginner_project.ticket_system.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsByTicketAndRecipientEmailAndNotificationTypeAndStatusAndInvalidatedFalse(
            Ticket ticket,
            String recipientEmail,
            NotificationType notificationType,
            NotificationStatus status
    );

    @Query("SELECT n FROM NotificationLog n WHERE n.status IN :statuses AND n.retryCount < :maxRetries")
List<NotificationLog> findByStatusInAndRetryCountLessThan(
        @Param("statuses") List<NotificationStatus> statuses,
        @Param("maxRetries") int maxRetries

);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE NotificationLog n SET n.invalidated = true WHERE n.ticket = :ticket AND n.notificationType IN :types")
    void invalidateByTicketAndNotificationTypeIn(
            @Param("ticket") Ticket ticket,
            @Param("types") List<NotificationType> types
    );
}