package com.beginner_project.ticket_system.service;

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
    public String login(String username, String password) {

        // DEBUG START
        System.out.println("=================================");
        System.out.println("LOGIN ATTEMPT: " + username);
        System.out.println("=================================");

        // authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // if authentication succeeds, this prints
        System.out.println("AUTH SUCCESS");

        // load user details again for token generation
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        System.out.println("JWT GENERATING...");

        // generate JWT
        return jwtService.generateToken(userDetails);
    }
}