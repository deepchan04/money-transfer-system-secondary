package com.training.mts.repository;

import com.training.mts.model.BankAccount;

import org.springframework.data.jpa.repository.JpaRepository;




public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {


    BankAccount findByAccountNumber(String accountNumber);

}

