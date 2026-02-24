package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.UserSignupRequest;
import com.beginner_project.ticket_system.entity.Users;

public interface UserService {

    void registerUser(UserSignupRequest request);

    Users getByUsername(String username);
}