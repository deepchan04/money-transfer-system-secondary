package com.training.mts.service;

import com.training.mts.exceptions.*;
import com.training.mts.model.BankAccount;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.BankAccountRepository;
import com.training.mts.repository.UserRepository;
import com.training.mts.repository.VPARepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankAccountServiceImpl implements BankAccountService{


    private BankAccountRepository bankAccountRepository;
    private VPARepository vpaRepository;
    private UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public BankAccountServiceImpl(PasswordEncoder passwordEncoder,BankAccountRepository bankAccountRepository,VPARepository vpaRepository,UserRepository userRepository){
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.vpaRepository = vpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User linkBankAccount(String vpaId, String accountNumber, String accountPassword) throws AccountNotFoundException, IncorrectPasswordException, AccountLinkedException {
        // 1. Find the user
        Optional<VPA> vpa = vpaRepository.findByVpaId(vpaId);
        if(!vpa.isPresent()){
            throw new VPAIdNotFoundException("VPA id "+vpaId+" not found");
        }

        User user = vpa.get().getUser();

        if(user.getBankAccount()!=null){
            throw new AccountLinkedException("Bank Account linked already!!");
        }
        // 2. Find the bank account (Read-only)
        BankAccount existingAccount = bankAccountRepository.findByAccountNumber(accountNumber);

        if (existingAccount == null) {
            throw new AccountNotFoundException("Bank account not found with account number: " + accountNumber);
        }

        String accPwd = existingAccount.getAccountPassword();

        if(accPwd.equals(accountPassword)){
            user.setBankAccount(existingAccount);
            return userRepository.save(user);
        }
        throw new IncorrectPasswordException("Account password incorrect!!");

        // 3. LINKING: Set the account on the User object
        // This will insert a row into the 'user_bank_accounts' mapping table


        // 4. Save the USER, not the bank account

    }

    public BankAccount getBankAccount(String vpaId) throws AccountNotLinkedException, IncorrectPasswordException {
        // Since User now holds the reference, you just get it from the user object
        Optional<VPA> vpa = vpaRepository.findByVpaId(vpaId);
        if(!vpa.isPresent()){
            throw new VPAIdNotFoundException("VPA id "+vpaId+" not found");
        }
        User user = vpa.get().getUser();


        if(user.getBankAccount() != null){
            return user.getBankAccount();
        }
        else {
            throw new AccountNotLinkedException("Account not linked");
        }




    }



    public void updateBalance(BankAccount bankAccount, Double newBalance) {
        bankAccount.setBalance(newBalance);
    }
}