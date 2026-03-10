package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.CommentRequest;
import com.beginner_project.ticket_system.dto.CommentResponse;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService)
    {
        this.commentService = commentService;
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment( @PathVariable Long id,@Valid @RequestBody CommentRequest request)
    {
        Users user = getCurrentUser();
        CommentResponse response = commentService.addComment(id, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
        @PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size)
    {
        Users user = getCurrentUser();
        return ResponseEntity.ok(commentService.getComments(id, page, size, user));
    }

    private Users getCurrentUser() {
        return (Users) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}