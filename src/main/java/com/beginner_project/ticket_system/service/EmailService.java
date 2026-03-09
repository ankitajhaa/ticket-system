package com.beginner_project.ticket_system.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);


    void sendEmailWithAttachment(String to, String subject,String body, byte[] attachment, String attachmentName);
}