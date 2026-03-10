package com.beginner_project.ticket_system.specification;

import com.beginner_project.ticket_system.dto.TicketFilterRequest;
import com.beginner_project.ticket_system.entity.Ticket;
import com.beginner_project.ticket_system.entity.Users;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TicketSpecification implements Specification<Ticket>
{

    private final TicketFilterRequest filter;
    private final Users currentUser;

    public TicketSpecification(TicketFilterRequest filter, Users currentUser)
    {
        this.filter = filter;
        this.currentUser = currentUser;
    }

    @Override
    public Predicate toPredicate(Root<Ticket> root, CriteriaQuery<?> query, CriteriaBuilder cb)
    {
        List<Predicate> predicates = new ArrayList<>();

        switch (currentUser.getRole())
        {
            case SUPPORT_AGENT ->
                predicates.add(cb.equal(root.get("assignedAgent"), currentUser));
            case CUSTOMER ->
                predicates.add(cb.equal(root.get("createdBy"), currentUser));
        }

        if (filter.getStatus() != null)
        {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getPriority() != null)
        {
            predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
        }
        if (filter.getAssignedAgentId() != null)
            {
            predicates.add(cb.equal(root.get("assignedAgent").get("id"), filter.getAssignedAgentId()));
        }
        if (filter.getCustomerId() != null)
        {
            predicates.add(cb.equal(root.get("createdBy").get("id"), filter.getCustomerId()));
        }
        if (filter.getSlaBreached() != null)
        {
            predicates.add(cb.equal(root.get("slaBreached"), filter.getSlaBreached()));
        }
        if (filter.getCreatedFrom() != null)
        {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
        }
        if (filter.getCreatedTo() != null)
        {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
        }
        if (filter.getDeadlineFrom() != null)
        {
            predicates.add(cb.greaterThanOrEqualTo(root.get("slaDeadline"), filter.getDeadlineFrom()));
        }
        if (filter.getDeadlineTo() != null)
        {
            predicates.add(cb.lessThanOrEqualTo(root.get("slaDeadline"), filter.getDeadlineTo()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}