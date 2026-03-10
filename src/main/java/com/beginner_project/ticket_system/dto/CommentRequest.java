package com.beginner_project.ticket_system.dto;

import com.beginner_project.ticket_system.enums.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentRequest {

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Comment type is required")
    private CommentType commentType;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public CommentType getCommentType() { return commentType; }
    public void setCommentType(CommentType commentType) { this.commentType = commentType; }
}