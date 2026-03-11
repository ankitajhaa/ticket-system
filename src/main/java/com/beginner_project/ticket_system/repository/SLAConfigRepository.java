package com.beginner_project.ticket_system.repository;

import com.beginner_project.ticket_system.entity.SLAConfig;
import com.beginner_project.ticket_system.enums.Priority;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SLAConfigRepository extends JpaRepository<SLAConfig, Long> {

    
    @Cacheable(value = "slaConfig", key = "#priority.name()")
    SLAConfig findByPriority(Priority priority);
}