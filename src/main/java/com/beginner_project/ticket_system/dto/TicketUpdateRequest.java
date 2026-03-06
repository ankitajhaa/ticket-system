package com.beginner_project.ticket_system.dto;

import com.beginner_project.ticket_system.enums.Action;
import com.beginner_project.ticket_system.enums.Priority;

public class TicketUpdateRequest {

    private Action action;
    private String status;
    private Long assignedAgent;
    private Priority priority;

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public String getStatus() 
    { 
        return status; 
    }
    public void setStatus(String status) 
    {
         this.status = status;   
    }

    public Long getAssignedAgent() 
    { 
        return assignedAgent; 
    }
    public void setAssignedAgent(Long assignedAgent)
     { 
        this.assignedAgent = assignedAgent; 
    }

    public Priority getPriority()
     { 
        return priority; 
    }
    public void setPriority(Priority priority) 
    { 
        this.priority = priority; 
    }
}