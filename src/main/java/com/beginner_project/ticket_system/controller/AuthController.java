package com.beginner_project.ticket_system.controller;

import com.beginner_project.ticket_system.dto.LoginRequest;
import com.beginner_project.ticket_system.dto.LoginResponse;
import com.beginner_project.ticket_system.dto.UserSignupRequest;
import com.beginner_project.ticket_system.service.AuthService;
import com.beginner_project.ticket_system.service.UserService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> signup(
            @RequestBody UserSignupRequest request) {

        userService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        String accessToken = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        String refreshToken = accessToken;

        long expiryTime = 1000 * 60 * 60;

        return ResponseEntity.ok(
                new LoginResponse(
                        accessToken,
                        refreshToken,
                        expiryTime
                )
        );
    }
}