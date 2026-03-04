package com.beginner_project.ticket_system.service;

import com.beginner_project.ticket_system.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

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

        logger.info("LOGIN ATTEMPT: {}", username);

        // authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        logger.info("Authentication successful for user: {}", username);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        logger.debug("JWT GENERATING...");

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        logger.debug("Access and refresh tokens generated");

        return new LoginResponse(
                accessToken,
                refreshToken,
                60   // expiry time in minutes
        );
    }
}