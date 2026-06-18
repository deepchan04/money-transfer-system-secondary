package com.training.mts.service;

import com.training.mts.enums.TransactionStatus;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class TransactionFailServiceImpl {
    private TransactionRepository transactionRepository;
    public TransactionFailServiceImpl(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction saveFailedTransaction(String key, User payer, User payee, Double amount, String reason){
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(key);
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setAmount(amount);
        transaction.setFailureReason(reason);
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setTransactionTime(LocalDateTime.now());
        return transactionRepository.save(transaction);

    }
}
