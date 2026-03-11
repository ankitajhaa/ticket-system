package com.beginner_project.ticket_system.dto;

import java.time.LocalDateTime;

public class MetricsResponse {

    private String period;
    private LocalDateTime from;
    private LocalDateTime to;
    private long totalTicketsCreated;
    private long totalTicketsResolved;
    private long totalSlaBreaches;
    private long totalActiveOpenTickets;
    private double averageResolutionTimeHours;

    public MetricsResponse(String period, LocalDateTime from, LocalDateTime to,
                           long totalTicketsCreated, long totalTicketsResolved,
                           long totalSlaBreaches, long totalActiveOpenTickets,
                           double averageResolutionTimeHours) {
        this.period = period;
        this.from = from;
        this.to = to;
        this.totalTicketsCreated = totalTicketsCreated;
        this.totalTicketsResolved = totalTicketsResolved;
        this.totalSlaBreaches = totalSlaBreaches;
        this.totalActiveOpenTickets = totalActiveOpenTickets;
        this.averageResolutionTimeHours = averageResolutionTimeHours;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public long getTotalTicketsCreated() {
        return totalTicketsCreated;
    }

    public void setTotalTicketsCreated(long totalTicketsCreated) {
        this.totalTicketsCreated = totalTicketsCreated;
    }

    public long getTotalTicketsResolved() {
        return totalTicketsResolved;
    }

    public void setTotalTicketsResolved(long totalTicketsResolved) {
        this.totalTicketsResolved = totalTicketsResolved;
    }

    public long getTotalSlaBreaches() {
        return totalSlaBreaches;
    }

    public void setTotalSlaBreaches(long totalSlaBreaches) {
        this.totalSlaBreaches = totalSlaBreaches;
    }

    public long getTotalActiveOpenTickets() {
        return totalActiveOpenTickets;
    }

    public void setTotalActiveOpenTickets(long totalActiveOpenTickets) {
        this.totalActiveOpenTickets = totalActiveOpenTickets;
    }

    public double getAverageResolutionTimeHours() {
        return averageResolutionTimeHours;
    }

    public void setAverageResolutionTimeHours(double averageResolutionTimeHours) {
        this.averageResolutionTimeHours = averageResolutionTimeHours;
    }
}
    