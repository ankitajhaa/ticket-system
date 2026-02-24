package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(String username, String password);
}