package com.training.mts.repository;


import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);

    Optional<User> findByvpa(VPA vpa);
    Optional<User> findByBankAccount(BankAccount bankAccount);
    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.name) LIKE LOWER(CONCAT(:query, '%'))
           OR u.phoneNumber LIKE CONCAT(:query, '%')
           OR LOWER(u.vpa.vpaId) LIKE LOWER(CONCAT(:query, '%'))
        """)
    List<User> searchUsers(String query);


}

