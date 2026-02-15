package com.training.mts.service;

import com.training.mts.dto.TransactionDTO;
import com.training.mts.dto.TransactionHistoryResponse;
import com.training.mts.dto.TransactionRequest;
import com.training.mts.enums.AppStatus;
import com.training.mts.enums.TransactionType;
import com.training.mts.exceptions.*;
import com.training.mts.model.BankAccount;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.TransactionRepository;
import com.training.mts.enums.TransactionStatus;

import com.training.mts.repository.VPARepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class TransactionServiceImpl implements TransactionService{


    private TransactionRepository transactionRepository;
    private BankAccountServiceImpl bankAccountService;
    private VPARepository vpaRepository;
    private PasswordEncoder passwordEncoder;
    private TransactionFailServiceImpl tFail;

    public TransactionServiceImpl(TransactionFailServiceImpl tFail,PasswordEncoder passwordEncoder,TransactionRepository transactionRepository, BankAccountServiceImpl bankAccountService, VPARepository vpaRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountService = bankAccountService;
        this.vpaRepository = vpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tFail = tFail;
    }

    @Transactional
    public TransactionDTO initiatePayment(TransactionRequest request) throws IncorrectPasswordException, AccountNotLinkedException {
        String key = request.getIdempotencyKey();

        // 1. Guard Clause for Idempotency
        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(key);
        if (existingTx.isPresent()) {
            return new TransactionDTO(handleIdempotency(existingTx.get(), request));
        }

        String reason = "Account of user";
        String vpa = "VPA id ";
        String notfound = "not found";
        // 2. Resolve Entities
        VPA payerVpa = vpaRepository.findByVpaId(request.getPayerVpaId())
                .orElseThrow(() -> new VPAIdNotFoundException(vpa+ request.getPayerVpaId() + notfound));
        VPA payeeVpa = vpaRepository.findByVpaId(request.getPayeeVpaId())
                .orElseThrow(() -> new VPAIdNotFoundException(vpa + request.getPayeeVpaId() + notfound));

        User payer = payerVpa.getUser();
        User payee = payeeVpa.getUser();

        if(!payer.getAppStatus().equals(AppStatus.ACTIVE)){
            tFail.saveFailedTransaction(request.getIdempotencyKey(),payer,payee, request.getAmount(), reason+payerVpa+" is not active");
            throw new AccountNotActiveException(reason+payerVpa.getVpaId()+" is not active");
        }

        if(!payee.getAppStatus().equals(AppStatus.ACTIVE)){
            tFail.saveFailedTransaction(request.getIdempotencyKey(),payer,payee, request.getAmount(), reason+payeeVpa+" is not active");
            throw new AccountNotActiveException(reason+payeeVpa.getVpaId()+" is not active");
        }


        // 3. Validation
        validateTransactionRequest(payer, request);

        // 4. Resolve Bank Accounts
        BankAccount payerAccount = getValidatedAccount(request,payer,payee,payer);
        BankAccount payeeAccount = getValidatedAccount(request,payer,payee,payee);

        // 5. Balance Check
        if (payerAccount.getBalance() < request.getAmount()) {
            tFail.saveFailedTransaction(request.getIdempotencyKey(),payer,payee, request.getAmount(), "Insufficient balance");
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 6. Execute Transfer and Save
        executeMoneyTransfer(payerAccount, payeeAccount, request.getAmount());

        return new TransactionDTO(saveTransactionRecord(key, payer, payee, request.getAmount(), request.getNote(), request.getTransactionType()));

    }




    private Transaction handleIdempotency(Transaction tx, TransactionRequest request) {
        if (!tx.getAmount().equals(request.getAmount()) ||
                !tx.getPayee().getVpa().getVpaId().equals(request.getPayeeVpaId())) {

            throw new IllegalStateException("Idempotency key reused with different parameters!");
        }
        return tx;
    }

    private void validateTransactionRequest(User payer, TransactionRequest request) throws IncorrectPasswordException {

        if(!passwordEncoder.matches(request.getPayerPwd(), payer.getPassword())) {

            throw new IncorrectPasswordException("Password Incorrect!!");
        }
        if (request.getAmount() <= 0) {

            throw new InvalidAmountException("Amount must be greater than 0");
        }
    }

    private BankAccount getValidatedAccount(TransactionRequest request, User payer, User payee, User user) throws AccountNotLinkedException {

        BankAccount account = bankAccountService.getBankAccount(user.getVpa().getVpaId());
        if (account == null) {
            tFail.saveFailedTransaction(request.getIdempotencyKey(),payer,payee, request.getAmount(), "Account of user "+user.getId()+" is not linked");
            throw new AccountNotLinkedException("Account of user " + user.getId() + " not linked");
        }
        return account;
    }

    private void executeMoneyTransfer(BankAccount payerAcc, BankAccount payeeAcc, Double amount) {
        payerAcc.setBalance(payerAcc.getBalance() - amount);
        bankAccountService.updateBalance(payerAcc, payerAcc.getBalance());

        payeeAcc.setBalance(payeeAcc.getBalance() + amount);
        bankAccountService.updateBalance(payeeAcc, payeeAcc.getBalance());
    }

    private Transaction saveTransactionRecord(String key, User payer, User payee, Double amount, String note, TransactionType tType) {
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(key);
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionType(tType);
        transaction.setNote(note);
        return transactionRepository.save(transaction);
    }

    public TransactionHistoryResponse getTransactionHistory(String vpaId) {
        // 1. Fetch the user safely
        User user = vpaRepository.findByVpaId(vpaId)
                .map(VPA::getUser)
                .orElseThrow(() -> new VPAIdNotFoundException("VPA id " + vpaId + " not found"));

        // 2. Call the specific methods
        // Credits: User is Payee + Status must be SUCCESS
        List<TransactionDTO> credits = transactionRepository
                .findByPayeeAndStatusOrderByTransactionTimeDesc(user, TransactionStatus.SUCCESS)
                .stream()
                .map(TransactionDTO::new) // Convert Entity to DTO
                .toList();
        List<TransactionDTO> debits = transactionRepository
                .findByPayerOrderByTransactionTimeDesc(user)
                .stream()
                .map(TransactionDTO::new) // Convert Entity to DTO
                .toList();


        return new TransactionHistoryResponse(credits, debits);
    }




}

