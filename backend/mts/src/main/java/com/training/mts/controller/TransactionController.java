package com.training.mts.controller;

import com.training.mts.dto.TransactionDTO;
import com.training.mts.dto.TransactionHistoryResponse;
import com.training.mts.dto.TransactionRequest;
import com.training.mts.exceptions.AccountNotLinkedException;
import com.training.mts.exceptions.IncorrectPasswordException;
import com.training.mts.model.Transaction;
import com.training.mts.service.AnalyticsService;
import com.training.mts.service.TransactionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {


    private TransactionServiceImpl transactionService;
    private AnalyticsService analyticsService;

    public TransactionController(AnalyticsService analyticsService, TransactionServiceImpl transactionService){
        this.transactionService = transactionService;
        this.analyticsService = analyticsService;
    }
    @PostMapping("/pay")
    public ResponseEntity<TransactionDTO> initiatePayment(@RequestBody TransactionRequest transactionRequest)
            throws IncorrectPasswordException, AccountNotLinkedException {
        TransactionDTO transactionDto = transactionService.initiatePayment(transactionRequest);
        analyticsService.triggerSnowflakeSync();
        return new ResponseEntity<>(transactionDto, HttpStatus.CREATED);
    }

    @GetMapping("/gettranslog")
    public ResponseEntity<TransactionHistoryResponse> getTransLog(@RequestParam String vpaId){
        return new ResponseEntity<>(transactionService.getTransactionHistory(vpaId), HttpStatus.OK);
    }


}

