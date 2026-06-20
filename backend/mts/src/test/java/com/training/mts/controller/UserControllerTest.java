package com.training.mts.controller;

import com.training.mts.dto.GetAllUsersRequest;
import com.training.mts.model.User;
import com.training.mts.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Diya");
        user.setPhoneNumber("9999999999");
    }

    @Test
    void getBalance_success() throws Exception {

        when(userService.getBankBalance("diya@upi", "pass"))
                .thenReturn(5000.0);

        ResponseEntity<Double> response =
                userController.getBalance("diya@upi", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5000.0, response.getBody());

        verify(userService).getBankBalance("diya@upi", "pass");
    }

    @Test
    void getUser_success() {

        when(userService.getUserByPhoneNumber("9999999999"))
                .thenReturn(user);

        ResponseEntity<com.training.mts.dto.UserResponse> response =
                userController.getUser("9999999999");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Diya", response.getBody().getName());
        assertEquals("9999999999", response.getBody().getPhoneNumber());
    }

    @Test
    void getUsers_success() {

        GetAllUsersRequest dto = new GetAllUsersRequest();
        dto.setName("Diya");

        when(userService.getAllUsers())
                .thenReturn(List.of(dto));

        ResponseEntity<List<GetAllUsersRequest>> response =
                userController.getUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getVpa_success() {

        when(userService.getVpaId("9999999999"))
                .thenReturn("diya@upi");

        ResponseEntity<String> response =
                userController.getVpa("9999999999");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("diya@upi", response.getBody());
    }
}
