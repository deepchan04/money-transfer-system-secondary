package com.training.mts.service;

import com.training.mts.dto.TransactionDTO;
import com.training.mts.dto.TransactionHistoryResponse;
import com.training.mts.dto.TransactionRequest;
import com.training.mts.enums.AppStatus;
import com.training.mts.enums.TransactionStatus;
import com.training.mts.enums.TransactionType;
import com.training.mts.exceptions.*;
import com.training.mts.model.BankAccount;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.TransactionRepository;
import com.training.mts.repository.VPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    BankAccount payerAccount;
    BankAccount payeeAccount;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountServiceImpl bankAccountService;

    @Mock
    private VPARepository vpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TransactionFailServiceImpl tFail;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User payer;
    private User payee;
    private VPA payerVpa;
    private VPA payeeVpa;

    @BeforeEach
    void setup() {

        payer = new User();
        payer.setAppStatus(AppStatus.ACTIVE);
        payer.setPassword("encoded");

        payee = new User();
        payee.setAppStatus(AppStatus.ACTIVE);
        payee.setPassword("encoded");

        payerVpa = new VPA();
        payerVpa.setVpaId("payer@upi");
        payerVpa.setUser(payer);

        payeeVpa = new VPA();
        payeeVpa.setVpaId("payee@upi");
        payeeVpa.setUser(payee);

        payer.setVpa(payerVpa);
        payee.setVpa(payeeVpa);

        payerAccount = new BankAccount();
        payerAccount.setBalance(100.0);

        payeeAccount = new BankAccount();
        payeeAccount.setBalance(0.0);
    }


    @Test
    void testInitiatePayment_success() throws Exception {

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("payee@upi");
        request.setPayerPwd("raw");
        request.setAmount(500.0);
        request.setIdempotencyKey("key1");
        request.setTransactionType(TransactionType.TRANSFER);

        BankAccount payerAcc = new BankAccount();
        payerAcc.setBalance(1000.0);

        BankAccount payeeAcc = new BankAccount();
        payeeAcc.setBalance(100.0);

        when(transactionRepository.findByIdempotencyKey("key1"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("payee@upi"))
                .thenReturn(Optional.of(payeeVpa));

        when(passwordEncoder.matches("raw", "encoded"))
                .thenReturn(true);

        when(bankAccountService.getBankAccount("payer@upi"))
                .thenReturn(payerAcc);

        when(bankAccountService.getBankAccount("payee@upi"))
                .thenReturn(payeeAcc);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDTO result = transactionService.initiatePayment(request);

        assertEquals(500.0, result.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testInitiatePayment_incorrectPassword() {

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("payee@upi");
        request.setPayerPwd("wrong");
        request.setAmount(100.0);
        request.setIdempotencyKey("key2");

        when(transactionRepository.findByIdempotencyKey("key2"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("payee@upi"))
                .thenReturn(Optional.of(payeeVpa));

        when(passwordEncoder.matches("wrong", "encoded"))
                .thenReturn(false);

        assertThrows(IncorrectPasswordException.class,
                () -> transactionService.initiatePayment(request));
    }

    @Test
    void testGetTransactionHistory_success() {

        Transaction tx = new Transaction();
        tx.setAmount(100.0);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setPayer(payer);
        tx.setPayee(payee);

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(transactionRepository
                .findByPayeeAndStatusOrderByTransactionTimeDesc(payer, TransactionStatus.SUCCESS))
                .thenReturn(List.of());

        when(transactionRepository
                .findByPayerOrderByTransactionTimeDesc(payer))
                .thenReturn(List.of(tx));

        TransactionHistoryResponse response =
                transactionService.getTransactionHistory("payer@upi");

        assertEquals(0, response.getCredits().size());
        assertEquals(1, response.getDebits().size());
    }
    @Test
    void testIdempotency_keyReusedWithDifferentAmount_shouldThrow() {

        Transaction existing = new Transaction();
        existing.setAmount(500.0);

        User payerUser = new User();
        VPA payerVpa1 = new VPA();
        payerVpa1.setVpaId("payer@upi");
        payerUser.setVpa(payerVpa1);

        User payeeUser = new User();
        VPA payeeVpa1 = new VPA();
        payeeVpa1.setVpaId("payee@upi");
        payeeUser.setVpa(payeeVpa1);

        existing.setPayer(payerUser);
        existing.setPayee(payeeUser);
        TransactionRequest request = new TransactionRequest();
        request.setAmount(999.0); // different
        request.setPayeeVpaId("payee@upi");
        request.setIdempotencyKey("key1");

        when(transactionRepository.findByIdempotencyKey("key1"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testInitiatePayment_invalidAmount_shouldThrow() {

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("payee@upi");
        request.setPayerPwd("raw");
        request.setAmount(0.0); // invalid
        request.setIdempotencyKey("key3");

        when(transactionRepository.findByIdempotencyKey("key3"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("payee@upi"))
                .thenReturn(Optional.of(payeeVpa));

        when(passwordEncoder.matches("raw", "encoded"))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testInitiatePayment_accountNotLinked_shouldThrow() {

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("payee@upi");
        request.setPayerPwd("raw");
        request.setAmount(100.0);
        request.setIdempotencyKey("key4");

        when(transactionRepository.findByIdempotencyKey("key4"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("payee@upi"))
                .thenReturn(Optional.of(payeeVpa));

        when(passwordEncoder.matches("raw", "encoded"))
                .thenReturn(true);

        when(bankAccountService.getBankAccount("payer@upi"))
                .thenReturn(null); // NOT LINKED

        assertThrows(AccountNotLinkedException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testGetTransactionHistory_withCreditsAndDebits() {

        Transaction creditTx = new Transaction();
        creditTx.setAmount(200.0);
        creditTx.setStatus(TransactionStatus.SUCCESS);
        creditTx.setPayer(payee);
        creditTx.setPayee(payer);

        Transaction debitTx = new Transaction();
        debitTx.setAmount(100.0);
        debitTx.setStatus(TransactionStatus.SUCCESS);
        debitTx.setPayer(payer);
        debitTx.setPayee(payee);

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(transactionRepository
                .findByPayeeAndStatusOrderByTransactionTimeDesc(payer, TransactionStatus.SUCCESS))
                .thenReturn(List.of(creditTx));

        when(transactionRepository
                .findByPayerOrderByTransactionTimeDesc(payer))
                .thenReturn(List.of(debitTx));

        TransactionHistoryResponse response =
                transactionService.getTransactionHistory("payer@upi");

        assertEquals(1, response.getCredits().size());
        assertEquals(1, response.getDebits().size());
    }
    @Test
    void testInitiatePayment_idempotencyKeyReuse_validSameData_shouldReturnExisting() {

        Transaction existing = new Transaction();
        existing.setAmount(100.0);
        existing.setStatus(TransactionStatus.SUCCESS);
        User payerUser = new User();
        VPA localPayerVpa = new VPA();
        localPayerVpa.setVpaId("payer@upi");
        payerUser.setVpa(localPayerVpa);

        User payeeUser = new User();
        VPA payeeVpa1 = new VPA();
        payeeVpa1.setVpaId("payee@upi");
        payeeUser.setVpa(payeeVpa1);

        existing.setPayer(payerUser);
        existing.setPayee(payeeUser);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(100.0);
        request.setPayeeVpaId("payee@upi");
        request.setIdempotencyKey("sameKey");

        when(transactionRepository.findByIdempotencyKey("sameKey"))
                .thenReturn(Optional.of(existing));

        TransactionDTO dto = transactionService.initiatePayment(request);

        assertNotNull(dto);
    }
    @Test
    void testInitiatePayment_payerNotActive_shouldThrow() {

        payer.setAppStatus(AppStatus.LOCKED);

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("payee@upi");
        request.setAmount(100.0);
        request.setPayerPwd("raw");
        request.setIdempotencyKey("keyInactive1");

        when(transactionRepository.findByIdempotencyKey("keyInactive1"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("payee@upi"))
                .thenReturn(Optional.of(payeeVpa));

        assertThrows(AccountNotActiveException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testInitiatePayment_payeeNotActive_shouldThrow() {

        payee.setAppStatus(AppStatus.LOCKED);

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("payee@upi");
        request.setAmount(100.0);
        request.setPayerPwd("raw");
        request.setIdempotencyKey("keyInactive2");

        when(transactionRepository.findByIdempotencyKey("keyInactive2"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("payee@upi"))
                .thenReturn(Optional.of(payeeVpa));

        assertThrows(AccountNotActiveException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testInitiatePayment_insufficientBalance_shouldThrow() {

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("payee@upi");
        request.setAmount(9999.0);
        request.setPayerPwd("raw");
        request.setIdempotencyKey("keyBalance");

        when(transactionRepository.findByIdempotencyKey("keyBalance"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("payee@upi"))
                .thenReturn(Optional.of(payeeVpa));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        when(bankAccountService.getBankAccount("payer@upi"))
                .thenReturn(payerAccount);

        when(bankAccountService.getBankAccount("payee@upi"))
                .thenReturn(payeeAccount);

        assertThrows(InsufficientBalanceException.class,
                () -> transactionService.initiatePayment(request));
    }

    @Test
    void testIdempotency_sameAmountDifferentPayee_shouldThrow() {

        Transaction existing = new Transaction();
        existing.setAmount(100.0);

        User payerUser = new User();
        VPA payerVpa1 = new VPA();
        payerVpa1.setVpaId("payer@upi");
        payerUser.setVpa(payerVpa1);

        User payeeUser = new User();
        VPA payeeVpa1 = new VPA();
        payeeVpa1.setVpaId("payee@upi");
        payeeUser.setVpa(payeeVpa1);

        existing.setPayer(payerUser);
        existing.setPayee(payeeUser);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(100.0);
        request.setPayeeVpaId("different@upi");
        request.setIdempotencyKey("keyMismatch");

        when(transactionRepository.findByIdempotencyKey("keyMismatch"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testInitiatePayment_payerVpaNotFound_shouldThrow() {

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("missing@upi");
        request.setPayeeVpaId("payee@upi");
        request.setIdempotencyKey("keyMissing1");

        when(transactionRepository.findByIdempotencyKey("keyMissing1"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("missing@upi"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testInitiatePayment_payeeVpaNotFound_shouldThrow() {

        TransactionRequest request = new TransactionRequest();
        request.setPayerVpaId("payer@upi");
        request.setPayeeVpaId("missing@upi");
        request.setIdempotencyKey("keyMissing2");

        when(transactionRepository.findByIdempotencyKey("keyMissing2"))
                .thenReturn(Optional.empty());

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(vpaRepository.findByVpaId("missing@upi"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transactionService.initiatePayment(request));
    }
    @Test
    void testGetTransactionHistory_noTransactions_shouldReturnEmptyLists() {

        when(vpaRepository.findByVpaId("payer@upi"))
                .thenReturn(Optional.of(payerVpa));

        when(transactionRepository
                .findByPayeeAndStatusOrderByTransactionTimeDesc(payer, TransactionStatus.SUCCESS))
                .thenReturn(List.of());

        when(transactionRepository
                .findByPayerOrderByTransactionTimeDesc(payer))
                .thenReturn(List.of());

        TransactionHistoryResponse response =
                transactionService.getTransactionHistory("payer@upi");

        assertTrue(response.getCredits().isEmpty());
        assertTrue(response.getDebits().isEmpty());
    }
    @Test
    void testGetTransactionHistory_vpaNotFound_shouldThrow() {

        when(vpaRepository.findByVpaId("missing@upi"))
                .thenReturn(Optional.empty());

        assertThrows(VPAIdNotFoundException.class,
                () -> transactionService.getTransactionHistory("missing@upi"));
    }

}
