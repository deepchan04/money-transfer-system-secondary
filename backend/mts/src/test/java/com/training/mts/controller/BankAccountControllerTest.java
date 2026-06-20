package com.training.mts.controller;

import com.training.mts.dto.LinkAccountRequest;
import com.training.mts.exceptions.AccountLinkedException;
import com.training.mts.exceptions.AccountNotFoundException;
import com.training.mts.exceptions.IncorrectPasswordException;
import com.training.mts.model.User;
import com.training.mts.service.BankAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountControllerTest {

    @Mock
    private BankAccountServiceImpl bankAccountService;

    @InjectMocks
    private BankAccountController bankAccountController;

    private LinkAccountRequest request;

    @BeforeEach
    void setUp() {
        request = new LinkAccountRequest();
        request.setVpaId("diya@upi");
        request.setAccountNumber("1234567890");
        request.setAccountPassword("accpass");
    }

    @Test
    void linkBankAccount_success() {

        User user = new User();
        user.setName("Diya");

        when(bankAccountService.linkBankAccount(
                request.getVpaId(),
                request.getAccountNumber(),
                request.getAccountPassword()))
                .thenReturn(user);

        ResponseEntity<com.training.mts.dto.UserResponse> response =
                bankAccountController.linkBankAccount(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Diya", response.getBody().getName());
    }

    @Test
    void linkBankAccount_accountNotFound() {

        when(bankAccountService.linkBankAccount(any(), any(), any()))
                .thenThrow(new AccountNotFoundException("Not found"));

        assertThrows(AccountNotFoundException.class, () ->
                bankAccountController.linkBankAccount(request));
    }

    @Test
    void linkBankAccount_accountAlreadyLinked() {

        when(bankAccountService.linkBankAccount(any(), any(), any()))
                .thenThrow(new AccountLinkedException("Already linked"));

        assertThrows(AccountLinkedException.class, () ->
                bankAccountController.linkBankAccount(request));
    }

    @Test
    void linkBankAccount_incorrectPassword() {

        when(bankAccountService.linkBankAccount(any(), any(), any()))
                .thenThrow(new IncorrectPasswordException("Wrong password"));

        assertThrows(IncorrectPasswordException.class, () ->
                bankAccountController.linkBankAccount(request));
    }
}
