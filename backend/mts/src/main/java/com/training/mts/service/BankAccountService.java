package com.training.mts.service;

import com.training.mts.exceptions.AccountLinkedException;
import com.training.mts.exceptions.AccountNotFoundException;
import com.training.mts.exceptions.AccountNotLinkedException;
import com.training.mts.exceptions.IncorrectPasswordException;
import com.training.mts.model.BankAccount;
import com.training.mts.model.User;

public interface BankAccountService {
    public User linkBankAccount(String vpaId, String accountNumber, String accountPassword) throws AccountNotLinkedException, AccountNotFoundException, IncorrectPasswordException, AccountLinkedException;
    public BankAccount getBankAccount(String vpaId) throws AccountNotLinkedException, IncorrectPasswordException;
}
