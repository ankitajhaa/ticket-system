package com.beginner_project.ticket_system.repository;

import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    List<Ticket> findByCreatedBy(Users user);

    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    List<Ticket> findByAssignedAgent(Users user);

    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    List<Ticket> findByStatus(Status status);

    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    List<Ticket> findByAssignedAgentAndStatus(Users user, Status status);

    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    List<Ticket> findByCreatedByAndStatus(Users user, Status status);

    @Override
    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    List<Ticket> findAll();
    
    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    @Query("SELECT t FROM Ticket t WHERE t.slaDeadline <:now AND t.slaBreached =false AND t.status NOT IN :excludedStatuses")
    List<Ticket>findTicketsBreachedAndNotNotified(
        @Param("now") LocalDateTime now, @Param("excludedStatuses") List<Status> excludedStatuses);

    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    @Query("SELECT t from Ticket t where t.slaDeadline >:now AND t.slaBreached = true AND t.status NOT IN :excludedStatuses")
    List<Ticket>findActiveTicketsWithUpcomingDeadline(
        @Param("now") LocalDateTime now, @Param("excludedStatuses") List<Status>excludedStatuses);
    
}
