package com.training.mts.service;

import com.training.mts.model.Transaction;
import com.training.mts.model.User;

public interface TransactionFailService {
    public Transaction saveFailedTransaction(String key, User payer, User payee, Double amount, String reason);
}
