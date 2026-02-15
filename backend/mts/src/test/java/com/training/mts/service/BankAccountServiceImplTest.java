package com.training.mts.service;

import com.training.mts.exceptions.*;
import com.training.mts.model.BankAccount;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.BankAccountRepository;
import com.training.mts.repository.UserRepository;
import com.training.mts.repository.VPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private VPARepository vpaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    private User user;
    private VPA vpa;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        vpa = new VPA();
        vpa.setVpaId("diya@upi");
        vpa.setUser(user);

        bankAccount = new BankAccount();
        bankAccount.setAccountNumber("123456");
        bankAccount.setAccountPassword("accpass");
    }

    @Test
    void linkBankAccount_success() {

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        when(bankAccountRepository.findByAccountNumber("123456"))
                .thenReturn(bankAccount);

        when(userRepository.save(user))
                .thenReturn(user);

        User result = bankAccountService.linkBankAccount(
                "diya@upi",
                "123456",
                "accpass"
        );

        assertNotNull(result);
        assertEquals(bankAccount, result.getBankAccount());
        verify(userRepository).save(user);
    }

    @Test
    void linkBankAccount_vpaNotFound() {

        when(vpaRepository.findByVpaId("invalid@upi"))
                .thenReturn(Optional.empty());

        assertThrows(VPAIdNotFoundException.class, () ->
                bankAccountService.linkBankAccount(
                        "invalid@upi",
                        "123456",
                        "accpass"
                ));
    }

    @Test
    void linkBankAccount_accountAlreadyLinked() {

        user.setBankAccount(bankAccount);

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        assertThrows(AccountLinkedException.class, () ->
                bankAccountService.linkBankAccount(
                        "diya@upi",
                        "123456",
                        "accpass"
                ));
    }

    @Test
    void linkBankAccount_accountNotFound() {

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        when(bankAccountRepository.findByAccountNumber("123456"))
                .thenReturn(null);

        assertThrows(AccountNotFoundException.class, () ->
                bankAccountService.linkBankAccount(
                        "diya@upi",
                        "123456",
                        "accpass"
                ));
    }

    @Test
    void linkBankAccount_incorrectPassword() {

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        when(bankAccountRepository.findByAccountNumber("123456"))
                .thenReturn(bankAccount);

        assertThrows(IncorrectPasswordException.class, () ->
                bankAccountService.linkBankAccount(
                        "diya@upi",
                        "123456",
                        "wrongpass"
                ));
    }
    @Test
    void getBankAccount_success() {

        user.setBankAccount(bankAccount);

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        BankAccount result = bankAccountService.getBankAccount("diya@upi");

        assertNotNull(result);
        assertEquals(bankAccount, result);
    }
    @Test
    void getBankAccount_vpaNotFound() {

        when(vpaRepository.findByVpaId("invalid@upi"))
                .thenReturn(Optional.empty());

        assertThrows(VPAIdNotFoundException.class, () ->
                bankAccountService.getBankAccount("invalid@upi"));
    }
    @Test
    void getBankAccount_accountNotLinked() {

        user.setBankAccount(null);

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        assertThrows(AccountNotLinkedException.class, () ->
                bankAccountService.getBankAccount("diya@upi"));
    }
    @Test
    void updateBalance_success() {

        bankAccount.setBalance(1000.0);

        bankAccountService.updateBalance(bankAccount, 2000.0);

        assertEquals(2000.0, bankAccount.getBalance());
    }

}
