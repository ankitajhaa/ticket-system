package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.*;
import com.beginner_project.ticket_system.entity.Users;


import java.util.List;

import org.springframework.data.domain.Page;

public interface TicketService {

    TicketResponse createTicket(TicketCreateRequest request, Users user);

    List<TicketResponse> getTicketsForUser(Users user, String status);

    TicketResponse getTicketById(Long id, Users user);

    TicketResponse updateTicket(Long ticketId, TicketUpdateRequest request, Users user);

    Page<TicketResponse>searchTickets(TicketFilterRequest filter,int page,int size,String sortBy,String sortDir,Users user);
}