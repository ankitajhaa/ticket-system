package com.beginner_project.ticket_system.dao;

import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
