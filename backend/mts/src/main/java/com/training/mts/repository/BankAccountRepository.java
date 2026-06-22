package com.training.mts.repository;

import com.training.mts.model.BankAccount;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {


    BankAccount findByAccountNumber(String accountNumber);

    /**
     * Locks the exact bank account row using its Primary Key ID.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankAccount b WHERE b.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);

}

