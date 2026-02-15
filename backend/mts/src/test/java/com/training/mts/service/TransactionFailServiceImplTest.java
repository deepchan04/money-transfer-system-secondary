package com.training.mts.service;

import com.training.mts.enums.TransactionStatus;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionFailServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionFailServiceImpl transactionFailService;

    @Test
    void testSaveFailedTransaction_success() {

        User payer = new User();
        User payee = new User();

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionFailService.saveFailedTransaction(
                "key123",
                payer,
                payee,
                500.0,
                "Insufficient balance"
        );

        assertNotNull(result);
        assertEquals("key123", result.getIdempotencyKey());
        assertEquals(payer, result.getPayer());
        assertEquals(payee, result.getPayee());
        assertEquals(500.0, result.getAmount());
        assertEquals("Insufficient balance", result.getFailureReason());
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertNotNull(result.getTransactionTime());

        verify(transactionRepository).save(any(Transaction.class));
    }
}
