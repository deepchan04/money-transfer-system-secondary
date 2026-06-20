package com.training.mts.controller;

import com.training.mts.dto.LoginRequest;
import com.training.mts.dto.LoginResponse;
import com.training.mts.dto.UserRequest;
import com.training.mts.model.User;
import com.training.mts.dto.UserResponse;
import com.training.mts.security.JwtUtils;
import com.training.mts.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import org.springframework.security.core.GrantedAuthority;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JwtUtils jwtUtils;
    public AuthController(AuthenticationManager authenticationManager,UserService userService,JwtUtils jwtUtils){
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getPhoneNumber(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 1. Safely handle the principal
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            // This handles both null and cases where the principal isn't UserDetails
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Generate token using the verified userDetails
        String jwt = jwtUtils.generateJwtToken(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(new LoginResponse(jwt, userDetails.getUsername(), roles));
    }

    @PostMapping("/register")
        public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest user) {

            User createdUser = userService.createUser(user.getName(), user.getPhoneNumber(), user.getEmail(),
                user.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(createdUser));

    }
}
