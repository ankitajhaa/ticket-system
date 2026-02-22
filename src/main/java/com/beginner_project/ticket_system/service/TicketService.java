package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;

import java.util.List;

public interface TicketService {

    Ticket createTicket(TicketCreateRequest request, Users user);

    List<Ticket> getTicketsForUser(Users user);

    Ticket getTicketById(Long id);
}
