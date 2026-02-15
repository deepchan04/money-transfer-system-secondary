package com.training.mts.service;

import com.training.mts.dto.TransactionDTO;
import com.training.mts.dto.TransactionHistoryResponse;
import com.training.mts.dto.TransactionRequest;
import com.training.mts.exceptions.AccountNotLinkedException;
import com.training.mts.exceptions.IncorrectPasswordException;


public interface TransactionService {
    public TransactionDTO initiatePayment(TransactionRequest transactionRequest) throws IncorrectPasswordException, AccountNotLinkedException;
    public TransactionHistoryResponse getTransactionHistory(String vpaId );
}
