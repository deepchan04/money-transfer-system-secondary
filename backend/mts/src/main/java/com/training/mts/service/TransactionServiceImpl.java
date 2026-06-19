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
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private RewardService rewardService;
    private final EmailServiceImpl emailServiceImpl;

    public TransactionServiceImpl(TransactionFailServiceImpl tFail, PasswordEncoder passwordEncoder, TransactionRepository transactionRepository, BankAccountServiceImpl bankAccountService, VPARepository vpaRepository, RewardService rewardService, EmailServiceImpl emailServiceImpl) {
        this.transactionRepository = transactionRepository;
        this.bankAccountService = bankAccountService;
        this.vpaRepository = vpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tFail = tFail;
        this.rewardService = rewardService;
        this.emailServiceImpl = emailServiceImpl;
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

        Transaction savedTx = saveTransactionRecord(key, payer, payee, request.getAmount(), request.getNote(), request.getTransactionType());
        rewardService.awardPointsForTransaction(savedTx);
        String accountNumber = payer.getBankAccount().getAccountNumber(); // Use your actual getter here
        String amount = String.valueOf(request.getAmount());
        String payee_vpa = payee.getVpa().getVpaId();
        String date = LocalDate.now().toString();
        String payerName = payer.getName();

        // 3. Call the helper methods to generate HTML and send the emails
        try {
            // Generate and send Debit alert to Payer
            String payerHtml = getPayerEmailTemplate(amount, accountNumber, payee_vpa, date);
            emailServiceImpl.sendEmail(payer.getEmail(), "MTS - Debit Alert", payerHtml);

            // Generate and send Credit alert to Payee
            String payeeHtml = getPayeeEmailTemplate(amount, payerName, date);
            emailServiceImpl.sendEmail(payee.getEmail(), "MTS - Credit Alert", payeeHtml);

        } catch (MessagingException e) {
            // Log the error but don't necessarily block the user's transaction receipt
            // depending on your business requirements
            System.err.println("Failed to send transaction emails: " + e.getMessage());
        }



        return new TransactionDTO(savedTx);

    }

    private String getPayerEmailTemplate(String amount, String account, String toVpa, String date) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                "  <div style='background-color: #1a73e8; padding: 20px; text-align: center; color: white;'>" +
                "    <h2 style='margin: 0;'>MTS</h2>" +
                "    <p style='margin: 5px 0 0 0; opacity: 0.9;'>Transaction Notification</p>" +
                "  </div>" +
                "  <div style='padding: 24px; color: #333333; line-height: 1.6;'>" +
                "    <p>Dear Customer,</p>" +
                "    <p>An amount of <strong style='color: #d93025; font-size: 18px;'>Rs. " + amount + "</strong> has been debited from your account.</p>" +
                "    <table style='width: 100%; border-collapse: collapse; margin: 20px 0; background-color: #f8f9fa; border-radius: 6px; overflow: hidden;'>" +
                "      <tr><td style='padding: 12px; border-bottom: 1px solid #eeeeee; color: #666;'>From Account</td><td style='padding: 12px; border-bottom: 1px solid #eeeeee; font-weight: bold;'>" + account + "</td></tr>" +
                "      <tr><td style='padding: 12px; border-bottom: 1px solid #eeeeee; color: #666;'>Sent To (VPA)</td><td style='padding: 12px; border-bottom: 1px solid #eeeeee; font-weight: bold;'>" + toVpa + "</td></tr>" +
                "      <tr><td style='padding: 12px; color: #666;'>Date</td><td style='padding: 12px; font-weight: bold;'>" + date + "</td></tr>" +
                "    </table>" +
                "    <p style='font-size: 13px; color: #666666; margin-top: 30px;'>If this transaction was not initiated by you, please block your account immediately.</p>" +
                "  </div>" +
                "</div>";
    }

    private String getPayeeEmailTemplate(String amount, String fromName, String date) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                "  <div style='background-color: #28a745; padding: 20px; text-align: center; color: white;'>" +
                "    <h2 style='margin: 0;'>MTS</h2>" +
                "    <p style='margin: 5px 0 0 0; opacity: 0.9;'>Money Received</p>" +
                "  </div>" +
                "  <div style='padding: 24px; color: #333333; line-height: 1.6;'>" +
                "    <p>Dear Customer,</p>" +
                "    <p>An amount of <strong style='color: #28a745; font-size: 18px;'>Rs. " + amount + "</strong> has been credited to your account.</p>" +
                "    <table style='width: 100%; border-collapse: collapse; margin: 20px 0; background-color: #f8f9fa; border-radius: 6px; overflow: hidden;'>" +
                "      <tr><td style='padding: 12px; border-bottom: 1px solid #eeeeee; color: #666;'>Received From</td><td style='padding: 12px; border-bottom: 1px solid #eeeeee; font-weight: bold;'>" + fromName + "</td></tr>" +
                "      <tr><td style='padding: 12px; color: #666;'>Date</td><td style='padding: 12px; font-weight: bold;'>" + date + "</td></tr>" +
                "    </table>" +
                "    <p style='font-size: 13px; color: #666666; margin-top: 30px;'>Thank you for using MTS.</p>" +
                "  </div>" +
                "</div>";
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
        // Fetch transactions as entities so we can ensure `points` is set before returning
        List<Transaction> creditTx = transactionRepository
            .findByPayeeAndStatusOrderByTransactionTimeDesc(user, TransactionStatus.SUCCESS);
        for (Transaction t : creditTx) {
            if (t.getPoints() == null || t.getPoints() <= 0) {
            t.setPoints(0);
            transactionRepository.save(t);
            }
        }
        List<TransactionDTO> credits = creditTx.stream()
            .map(TransactionDTO::new)
            .toList();

        List<Transaction> debitTx = transactionRepository
            .findByPayerOrderByTransactionTimeDesc(user);
        for (Transaction t : debitTx) {
            if (t.getPoints() == null || t.getPoints() <= 0) {
            t.setPoints(0);
            transactionRepository.save(t);
            }
        }
        List<TransactionDTO> debits = debitTx.stream()
            .map(TransactionDTO::new)
            .toList();


        return new TransactionHistoryResponse(credits, debits);
    }




}

