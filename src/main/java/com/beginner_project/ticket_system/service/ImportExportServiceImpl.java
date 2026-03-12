package com.beginner_project.ticket_system.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.beginner_project.ticket_system.dto.TicketCreateRequest;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Priority;
import com.beginner_project.ticket_system.repository.TicketRepository;
import com.beginner_project.ticket_system.repository.UserRepository;
import com.beginner_project.ticket_system.util.NotificationTemplates;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


@Service
public class ImportExportServiceImpl implements ImportExportService {
 
     private static final Logger logger = LoggerFactory.getLogger(ImportExportServiceImpl.class);
     private final UserRepository userRepository;
     private final TicketService ticketService;
     private final TicketRepository ticketRepository;
     private final EmailService emailService;

    public ImportExportServiceImpl(
                                   UserRepository userRepository,
                                   TicketService ticketService, TicketRepository ticketRepository, 
                                   EmailService emailService ) {
       
        this.userRepository=userRepository;
        this.ticketService=ticketService;
        this.ticketRepository=ticketRepository;
        this.emailService=emailService;
    }
    @Async
    @Override
    public void importTicketsFromCSV(byte[] fileBytes, Users admin)
    {
      int totalRows=0;
      int SuccessRowReads=0;
      List<String> failedRows=new ArrayList<>();

      //lets try reading csv here now 
      try(CSVReader reader = new CSVReader(new InputStreamReader(new java.io.ByteArrayInputStream(fileBytes))))
      {
      String[] headers = reader.readNext();

      if(headers==null)
      {
       emailService.sendEmail(
    admin.getEmail(),
    NotificationTemplates.importFailedSubject(),
    NotificationTemplates.importFailedBody(admin.getUsername(), "CSV file is empty"));
         return ;
      }
      //array to store each columns
      String[] nextline;
      int rowNumber=1;
      while((nextline=reader.readNext()) != null)
      {
       totalRows++;
       rowNumber++;
       try{
         if(nextline.length< 4)
         {
           failedRows.add("Row" +rowNumber+":- Failed due to insufficient columns");
           continue;
         }

           String customerReference=nextline[0].trim();
           String title =nextline[1].trim();
           String description=nextline[2].trim();
           String priority=nextline[3].trim();

                    if (customerReference.isEmpty())
                    {
                        failedRows.add("Row " + rowNumber + ":customerReference is required");
                        continue;
                    }
                    if (title.isEmpty())
                        {
                        failedRows.add("Row " + rowNumber + ":title is required");
                        continue;
                    }
                    if (description.isEmpty()) {
                        failedRows.add("Row " + rowNumber + ":description is required");
                        continue;
                    }

                    Priority priorityEnum;
                    
                try{
                   if(priority.isEmpty())
                   {
                    priorityEnum=Priority.MEDIUM;

                   }
                   else
                   {
                    priorityEnum=Priority.valueOf(priority.toUpperCase());
                   }
                }catch (IllegalArgumentException e)
                {
                    failedRows.add("RowNumber " +rowNumber+":-Wrong Priority");
                    continue;
                }

                Users customer=userRepository.findByUsername(customerReference).orElse(null);


                 if (customer == null)
                    {
                        failedRows.add("Row " + rowNumber + ": customer '" + customerReference + "' not found");
                        continue;
                    }

                TicketCreateRequest ticketRequest = new TicketCreateRequest();
                ticketRequest.setTitle(title);
                ticketRequest.setDescription(description);
                ticketRequest.setPriority(priorityEnum);
                ticketService.createTicket(ticketRequest, customer);
                SuccessRowReads++;
                }catch(Exception e)
                {
                    failedRows.add("Row"+rowNumber+"- : Unable to read due to " +e.getMessage());
                    continue;
                }
        }
        int failedCount=failedRows.size();

        emailService.sendEmail(
    admin.getEmail(),
    NotificationTemplates.importCompleteSubject(),
    NotificationTemplates.importCompleteBody(admin.getUsername(), totalRows, SuccessRowReads, failedCount, failedRows)
);

       logger.info("Import complete. Total: {}, Success: {}, Failed: {}",
                    totalRows, SuccessRowReads, failedCount);

       }
       catch (Exception e) {
            logger.error("Import failed: {}", e.getMessage());
            emailService.sendEmail(
    admin.getEmail(),
    NotificationTemplates.importFailedSubject(),
    NotificationTemplates.importFailedBody(admin.getUsername(), e.getMessage())
);
        }


     
    }

    @Async
    @Override
    public void exportTickets(String type, Users admin) {

        try {
            List<Ticket> tickets;

            if (type.equalsIgnoreCase("SLA_BREACHED"))
            {
                tickets = ticketRepository.findBySlaBreached(true);
            } else
                {
                tickets = ticketRepository.findAll();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));

            writer.writeNext(new String[]{
                    "id", "title", "description", "status", "priority",
                    "assignedAgent", "createdBy", "slaDeadline", "createdAt"
            });
            for (Ticket ticket : tickets)
                {
                writer.writeNext(new String[]{
                        String.valueOf(ticket.getId()),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getStatus().name(),
                        ticket.getPriority() != null?ticket.getPriority().name() : "",
                        ticket.getAssignedAgent()==null? "" : ticket.getAssignedAgent().getUsername(),
                        ticket.getCreatedBy().getUsername(),
                        ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().toString() : "",
                        ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : ""
                });
            }

            writer.flush();
            writer.close();

          
            String subject = NotificationTemplates.exportCompleteSubject(type);
            String body = NotificationTemplates.exportCompleteBody(admin.getUsername(),type, tickets.size(),LocalDateTime.now().toString());
            byte[] csvBytes = outputStream.toByteArray();

            emailService.sendEmailWithAttachment(
                    admin.getEmail(),
                    subject,
                    body,
                    csvBytes,
                     "tickets_export_" + type + "_" + LocalDateTime.now().toLocalDate() + ".csv"
            );

            logger.info("Export complete. Type: {}, Records: {}", type, tickets.size());

        } catch (Exception e) {
            logger.error("Export failed: {}", e.getMessage());
           emailService.sendEmail(
    admin.getEmail(),
    NotificationTemplates.exportFailedSubject(),
    NotificationTemplates.exportFailedBody(admin.getUsername(), "Export Failed", e.getMessage())
);
        }
    }
}



