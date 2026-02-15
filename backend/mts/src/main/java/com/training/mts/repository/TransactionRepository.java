package com.training.mts.repository;
import com.training.mts.enums.TransactionStatus;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        List<Transaction> findByPayer(User payer);
        List<Transaction> findByPayee(User payee);
        // Transactions where user received money (Credit)
        List<Transaction> findByPayeeAndStatusOrderByTransactionTimeDesc(User payee, TransactionStatus status);

        // Transactions where user sent money (Debit)
        List<Transaction> findByPayerOrderByTransactionTimeDesc(User payer);
        List<Transaction> findByPayerOrPayeeOrderByTransactionTimeDesc(User payer, User payee);
        Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

}