package com.beginner_project.ticket_system.util;

import com.beginner_project.ticket_system.entity.Ticket;


public class NotificationTemplates {

    // ── SLA WARNING
    public static String slaWarningSubject(Ticket ticket) {
        return "[WARNING] Ticket #" + ticket.getId() + " - SLA deadline approaching";
    }

    public static String slaWarningBody(Ticket ticket, double hoursRemaining) {
        return "Hi " + ticket.getAssignedAgent().getUsername() + ",\n\n" +
               "This is a reminder that the following ticket is approaching its SLA deadline.\n\n" +
               "Ticket ID  : " + ticket.getId() + "\n" +
               "Title      : " + ticket.getTitle() + "\n" +
               "Priority   : " + ticket.getPriority() + "\n" +
               "Status     : " + ticket.getStatus() + "\n" +
               "Deadline   : " + ticket.getSlaDeadline() + "\n" +
               "Time Left  : " + hoursRemaining + " hours\n\n" +
               "Please take action immediately.\n\n" +
               "— Support System";
    }

    // ── SLA BREACH 
    public static String slaBreachSubject(Ticket ticket) {
        return "[BREACHED] Ticket #" + ticket.getId() + " - SLA deadline missed";
    }

    public static String slaBreachBody(Ticket ticket, String recipientName) {
        return "Hi " + recipientName + ",\n\n" +
               "The following ticket has breached its SLA deadline.\n\n" +
               "Ticket ID     : " + ticket.getId() + "\n" +
               "Title         : " + ticket.getTitle() + "\n" +
               "Priority      : " + ticket.getPriority() + "\n" +
               "Status        : " + ticket.getStatus() + "\n" +
               "Deadline      : " + ticket.getSlaDeadline() + "\n" +
               "Assigned Agent: " + (ticket.getAssignedAgent() != null
                   ? ticket.getAssignedAgent().getUsername() : "Unassigned") + "\n\n" +
               "Immediate action required.\n\n" +
               "— Support System";
    }

    // ── TICKET ASSIGNED — CUSTOMER 
    public static String ticketAssignedCustomerSubject(Ticket ticket) {
        return "[ASSIGNED] Ticket #" + ticket.getId() + " has been assigned";
    }

    public static String ticketAssignedCustomerBody(Ticket ticket) {
        return "Hi " + ticket.getCreatedBy().getUsername() + ",\n\n" +
               "Your support ticket has been assigned to an agent.\n\n" +
               "Ticket ID : " + ticket.getId() + "\n" +
               "Title     : " + ticket.getTitle() + "\n" +
               "Priority  : " + ticket.getPriority() + "\n" +
               "Status    : ASSIGNED\n" +
               "Agent     : " + ticket.getAssignedAgent().getUsername() + "\n\n" +
               "We will keep you updated on progress.\n\n" +
               "— Support System";
    }

    // ── TICKET ASSIGNED — AGENT 
    public static String ticketAssignedAgentSubject(Ticket ticket) {
        return "[ASSIGNED] Ticket #" + ticket.getId() + " has been assigned to you";
    }

    public static String ticketAssignedAgentBody(Ticket ticket) {
        return "Hi " + ticket.getAssignedAgent().getUsername() + ",\n\n" +
               "A new ticket has been assigned to you.\n\n" +
               "Ticket ID   : " + ticket.getId() + "\n" +
               "Title       : " + ticket.getTitle() + "\n" +
               "Priority    : " + ticket.getPriority() + "\n" +
               "Status      : ASSIGNED\n" +
               "Customer    : " + ticket.getCreatedBy().getUsername() + "\n" +
               "SLA Deadline: " + ticket.getSlaDeadline() + "\n\n" +
               "Please review and take action.\n\n" +
               "— Support System";
    }

    // ── PRIORITY CHANGED 
    public static String priorityChangedSubject(Ticket ticket) {
        return "[PRIORITY CHANGE] Ticket #" + ticket.getId()
                + " priority updated to " + ticket.getPriority();
    }

    public static String priorityChangedBody(Ticket ticket,
                                               String oldPriority,
                                               String oldDeadline) {
        return "Hi " + ticket.getAssignedAgent().getUsername() + ",\n\n" +
               "The priority of an assigned ticket has been changed.\n\n" +
               "Ticket ID       : " + ticket.getId() + "\n" +
               "Title           : " + ticket.getTitle() + "\n" +
               "Old Priority    : " + oldPriority + "\n" +
               "New Priority    : " + ticket.getPriority() + "\n" +
               "Old SLA Deadline: " + oldDeadline + "\n" +
               "New SLA Deadline: " + ticket.getSlaDeadline() + "\n\n" +
               "Please review your updated deadline.\n\n" +
               "— Support System";
    }

    // ── IMPORT COMPLETE 
    public static String importCompleteSubject() {
        return "[IMPORT COMPLETE] CSV Import Summary";
    }

    public static String importCompleteBody(String adminName, int total,
                                             int success, int failed,
                                             String failureDetails) {
        return "Hi " + adminName + ",\n\n" +
               "Your bulk ticket import has been completed.\n\n" +
               "Total Records : " + total + "\n" +
               "Successful    : " + success + "\n" +
               "Failed        : " + failed + "\n\n" +
               (failureDetails.isEmpty() ? "" : "Failed Rows:\n" + failureDetails + "\n") +
               "— Support System";
    }

    // ── IMPORT FAILED 
    public static String importFailedSubject() {
        return "[FAILED] CSV Import could not be completed";
    }

    public static String importFailedBody(String adminName, String reason) {
        return "Hi " + adminName + ",\n\n" +
               "Your CSV import request has failed and could not be completed.\n\n" +
               "Operation   : BULK IMPORT\n" +
               "Reason      : " + reason + "\n\n" +
               "Please try again or contact system support if the issue persists.\n\n" +
               "— Support System";
    }

    // ── EXPORT COMPLETE 
    public static String exportCompleteSubject(String exportType) {
        return "[EXPORT COMPLETE] Ticket Export - " + exportType;
    }

    public static String exportCompleteBody(String adminName,
                                             String exportType,
                                             int totalRows,
                                             String generatedAt) {
        return "Hi " + adminName + ",\n\n" +
               "Your ticket export has been completed.\n" +
               "Please find the CSV file attached.\n\n" +
               "Export Type : " + exportType + "\n" +
               "Total Rows  : " + totalRows + "\n" +
               "Generated At: " + generatedAt + "\n\n" +
               "— Support System";
    }

    // ── EXPORT FAILED 
    public static String exportFailedSubject() {
        return "[FAILED] Ticket Export could not be completed";
    }

    public static String exportFailedBody(String adminName,
                                           String exportType,
                                           String reason) {
        return "Hi " + adminName + ",\n\n" +
               "Your export request has failed and could not be completed.\n\n" +
               "Operation   : " + exportType + " EXPORT\n" +
               "Reason      : " + reason + "\n\n" +
               "Please try again or contact system support if the issue persists.\n\n" +
               "— Support System";
    }
}