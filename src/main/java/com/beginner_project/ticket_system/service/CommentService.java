package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.CommentRequest;
import com.beginner_project.ticket_system.dto.CommentResponse;
import com.beginner_project.ticket_system.entity.Users;
import org.springframework.data.domain.Page;

public interface CommentService {

    CommentResponse addComment(Long ticketId, CommentRequest request, Users user);

    Page<CommentResponse> getComments(Long ticketId, int page, int size, Users user);
}