package com.beginner_project.ticket_system.entity;

import com.beginner_project.ticket_system.enums.Priority;
import jakarta.persistence.*;

@Entity
@Table(name = "SLA_config")
public class SLAConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, unique = true)
    private Priority priority;

    @Column(name = "resolution_hours", nullable = false)
    private Integer resolutionHours;

    @Column(name = "reminder_hours", nullable = false)
    private String reminderHours;

    public SLAConfig() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getResolutionHours() {
        return resolutionHours;
    }

    public void setResolutionHours(Integer resolutionHours) {
        this.resolutionHours = resolutionHours;
    }

    public String getReminderHours() {
        return reminderHours;
    }

    public void setReminderHours(String reminderHours) {
        this.reminderHours = reminderHours;
    }

    
}