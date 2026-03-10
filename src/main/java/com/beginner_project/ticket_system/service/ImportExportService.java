package com.beginner_project.ticket_system.service;

import org.springframework.web.multipart.MultipartFile;

import com.beginner_project.ticket_system.entity.Users;

public interface ImportExportService {
   
    void importTicketsFromCSV(byte[] fileBytes ,Users admin);

    void exportTickets(String type, Users admin);
}
