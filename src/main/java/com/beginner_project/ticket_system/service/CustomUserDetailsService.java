package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dao.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
