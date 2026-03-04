package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.repository.UserRepository;
import com.beginner_project.ticket_system.dto.UserSignupRequest;
import com.beginner_project.ticket_system.entity.Users;
import com.beginner_project.ticket_system.enums.Role;
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
    public void registerUser(UserSignupRequest request) {

        Users user = new Users();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());   // ← YOU FORGOT THIS

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.CUSTOMER);

        userRepository.save(user);
    }

    @Override
    public Users getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}