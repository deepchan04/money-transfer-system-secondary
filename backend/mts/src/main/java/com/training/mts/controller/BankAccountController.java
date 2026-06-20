package com.training.mts.controller;

import com.training.mts.dto.LinkAccountRequest;
import com.training.mts.exceptions.AccountLinkedException;
import com.training.mts.exceptions.AccountNotFoundException;
import com.training.mts.exceptions.IncorrectPasswordException;
import com.training.mts.model.User;
import com.training.mts.dto.UserResponse;
import com.training.mts.service.BankAccountServiceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bankaccounts")
public class BankAccountController {

    private final BankAccountServiceImpl bankAccountService;

    // Spring will automatically inject this
    public BankAccountController(BankAccountServiceImpl bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping("/link")
    public ResponseEntity<UserResponse> linkBankAccount(@RequestBody LinkAccountRequest linkAccountRequest) throws AccountNotFoundException, IncorrectPasswordException, AccountLinkedException {
        User created = bankAccountService.linkBankAccount(linkAccountRequest.getVpaId(), linkAccountRequest.getAccountNumber(), linkAccountRequest.getAccountPassword());
        return new ResponseEntity<>(UserResponse.from(created),HttpStatus.CREATED);
    }


}

