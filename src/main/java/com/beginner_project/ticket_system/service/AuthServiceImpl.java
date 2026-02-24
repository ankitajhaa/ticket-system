package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(String username, String password) {

        System.out.println("LOGIN ATTEMPT: " + username);

        // authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        System.out.println("AUTH SUCCESS");

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        System.out.println("JWT GENERATING...");

        String accessToken = jwtService.generateToken(userDetails);

        // TEMP refresh token (real one later)
        String refreshToken = accessToken;

        return new LoginResponse(
                accessToken,
                refreshToken,
                60   // expiry time in minutes
        );
    }
}