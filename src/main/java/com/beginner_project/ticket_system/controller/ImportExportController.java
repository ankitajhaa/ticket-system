package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.exception.BusinessException;
import com.beginner_project.ticket_system.service.ImportExportService;



import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/tickets")
public class ImportExportController {

    private final ImportExportService importExportService;

    public ImportExportController(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @PostMapping(value= "/import" , consumes=MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<?> importTickets(
            @RequestParam("file") MultipartFile file
    ) {
        Users admin = getCurrentUser();

        if (admin.getRole() != Role.ADMIN)
            throw new BusinessException("Admin only", HttpStatus.FORBIDDEN);

        if (file.isEmpty())
            throw new BusinessException("File is empty", HttpStatus.BAD_REQUEST);

        if (file.getOriginalFilename() == null ||
                !file.getOriginalFilename().endsWith(".csv"))
            throw new BusinessException(
                    "Only CSV files are supported", HttpStatus.BAD_REQUEST);

        try{
            byte[] fileBytes = file.getBytes();
            importExportService.importTicketsFromCSV(fileBytes, admin);
        }catch(Exception e)
        {
            throw new BusinessException(
                    "Failed to read file: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
                   

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "File received, processing in background"));
    }

    @PostMapping("/export")
    public ResponseEntity<?> exportTickets(
            @RequestParam String type
    ) {
        Users admin = getCurrentUser();

        if (admin.getRole() != Role.ADMIN)
            throw new BusinessException("Admin only", HttpStatus.FORBIDDEN);

        if (!type.equalsIgnoreCase("ALL") &&
                !type.equalsIgnoreCase("SLA_BREACHED"))
            throw new BusinessException(
                    "Invalid type. Must be ALL or SLA_BREACHED",
                    HttpStatus.BAD_REQUEST);

        importExportService.exportTickets(type, admin);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message",
                        "Export is being processed, you will receive an email shortly"));
    }

    private Users getCurrentUser() {
        return (Users) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}