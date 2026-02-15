package com.training.mts.controller;

import com.training.mts.dto.LoginRequest;
import com.training.mts.dto.LoginResponse;
import com.training.mts.dto.UserRequest;
import com.training.mts.model.User;
import com.training.mts.security.JwtUtils;
import com.training.mts.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setPhoneNumber("9999999999");
        loginRequest.setPassword("password");
    }

    @Test
    void authenticateUser_success() {

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(userDetails.getUsername())
                .thenReturn("9999999999");

        when((Collection<GrantedAuthority>) userDetails.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtUtils.generateJwtToken("9999999999"))
                .thenReturn("mock-jwt-token");

        ResponseEntity<LoginResponse> response =
                authController.authenticateUser(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().token());
        assertEquals("9999999999", response.getBody().username());
        assertEquals("ROLE_USER", response.getBody().roles().get(0));
    }

    @Test
    void authenticateUser_invalidPrincipal() {

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn("invalidPrincipal");

        ResponseEntity<LoginResponse> response =
                authController.authenticateUser(loginRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void registerUser_success() {

        UserRequest request = new UserRequest();
        request.setName("Diya");
        request.setPhoneNumber("9999999999");
        request.setEmail("diya@test.com");
        request.setPassword("password");

        User user = new User();
        user.setName("Diya");
        user.setPhoneNumber("9999999999");
        user.setEmail("diya@test.com");

        when(userService.createUser(any(), any(), any(), any()))
                .thenReturn(user);

        ResponseEntity<User> response =
                authController.registerUser(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Diya", response.getBody().getName());
        assertEquals("9999999999", response.getBody().getPhoneNumber());
        assertEquals("diya@test.com", response.getBody().getEmail());
    }
}
