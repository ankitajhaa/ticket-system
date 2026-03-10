package com.beginner_project.ticket_system.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private String author;
    private String content;
    private String commentType;
    private LocalDateTime createdAt;

    public CommentResponse(Long id, String author, String content,
                           String commentType, LocalDateTime createdAt) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.commentType = commentType;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

  
}