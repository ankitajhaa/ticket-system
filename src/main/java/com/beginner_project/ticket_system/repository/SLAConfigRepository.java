package com.beginner_project.ticket_system.repository;

import com.beginner_project.ticket_system.entity.SLAConfig;
import com.beginner_project.ticket_system.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SLAConfigRepository extends JpaRepository<SLAConfig, Long> {
    SLAConfig findByPriority(Priority priority);
}