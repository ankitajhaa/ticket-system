package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.*;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Status;

import java.util.List;

public interface TicketService {

    TicketResponse createTicket(TicketCreateRequest request, Users user);

    List<TicketResponse> getTicketsForUser(Users user, String status);

    TicketResponse getTicketById(Long id, Users user);

    TicketResponse assignToSelf(Long ticketId, Users agent);

    TicketResponse updateTicket(Long ticketId, TicketUpdateRequest request, Users user);
}