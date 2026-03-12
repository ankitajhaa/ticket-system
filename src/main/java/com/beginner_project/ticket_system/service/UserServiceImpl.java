package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.repository.UserRepository;
import com.beginner_project.ticket_system.dto.UpdateRoleRequest;
import com.beginner_project.ticket_system.dto.UserResponse;
import com.beginner_project.ticket_system.dto.UserSignupRequest;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
import com.beginner_project.ticket_system.exception.BusinessException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse registerUser(UserSignupRequest request) {

        Users user = new Users();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.CUSTOMER);

        Users saved= userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }
     

    @Override
    public UserResponse registerSupportAgent(UserSignupRequest request) {
         

         if (userRepository.existsByEmail(request.getEmail()))
        {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername()))
        {
            throw new RuntimeException("Username already taken");
        }
        Users user = new Users();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.SUPPORT_AGENT);
        Users saved=userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    @Override
public UserResponse updateUserRole(Long userId, UpdateRoleRequest request)
{
    if (request.getRole() == Role.ADMIN)
    throw new BusinessException("Cannot assign ADMIN role via this endpoint", HttpStatus.FORBIDDEN);
    Users user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    
    user.setRole(request.getRole());
    Users saved = userRepository.save(user);

    return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
}

    @Override
    public Users getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}