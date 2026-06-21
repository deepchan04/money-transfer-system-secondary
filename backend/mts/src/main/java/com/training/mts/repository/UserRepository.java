package com.training.mts.repository;


import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);

    Optional<User> findByvpa(VPA vpa);
    Optional<User> findByBankAccount(BankAccount bankAccount);


}

