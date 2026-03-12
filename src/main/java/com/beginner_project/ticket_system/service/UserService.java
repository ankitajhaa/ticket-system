package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.UpdateRoleRequest;
import com.beginner_project.ticket_system.dto.UserResponse;
import com.beginner_project.ticket_system.dto.UserSignupRequest;
import com.beginner_project.ticket_system.entity.Users;


public interface UserService {

    UserResponse registerUser(UserSignupRequest request);
    
    UserResponse registerSupportAgent(UserSignupRequest request);

    UserResponse updateUserRole(Long userId, UpdateRoleRequest request);
    Users getByUsername(String username);
}