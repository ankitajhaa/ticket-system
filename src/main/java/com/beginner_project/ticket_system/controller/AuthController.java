package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.LoginRequest;
import com.beginner_project.ticket_system.dto.LoginResponse;
import com.beginner_project.ticket_system.dto.UpdateRoleRequest;
import com.beginner_project.ticket_system.dto.UserResponse;
import com.beginner_project.ticket_system.dto.UserSignupRequest;
import com.beginner_project.ticket_system.service.AuthService;
import com.beginner_project.ticket_system.service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService,
                          UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup( @RequestBody UserSignupRequest request) {

        UserResponse user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/create-support-agent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createSupportAgent(@Valid @RequestBody UserSignupRequest request)
{
        UserResponse user = userService.registerSupportAgent(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserResponse> updateUserRole(
        @PathVariable("id") Long userId,
        @Valid @RequestBody UpdateRoleRequest request) {

    UserResponse response = userService.updateUserRole(userId, request);
    return ResponseEntity.ok(response);
}


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        return ResponseEntity.ok(response);
    }
}