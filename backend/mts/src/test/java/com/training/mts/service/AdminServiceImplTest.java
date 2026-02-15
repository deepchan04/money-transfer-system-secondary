package com.training.mts.service;

import com.training.mts.enums.AppStatus;
import com.training.mts.exceptions.VPAIdNotFoundException;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.TransactionRepository;
import com.training.mts.repository.UserRepository;
import com.training.mts.repository.VPARepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private VPARepository vpaRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User user;
    private VPA vpa;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setAppStatus(AppStatus.LOCKED);

        vpa = new VPA();
        vpa.setUser(user);
    }

    @Test
    void getTransactionHistory_shouldReturnAllTransactions() {

        Transaction transaction = new Transaction();
        transaction.setAmount(500.0);

        given(transactionRepository.findAll())
                .willReturn(List.of(transaction));

        List<Transaction> result = adminService.getTransactionHistory();

        assertEquals(1, result.size());
        assertEquals(500.0, result.get(0).getAmount());

        verify(transactionRepository).findAll();
    }

    @Test
    void findAll_shouldReturnAllUsers() {

        given(userRepository.findAll())
                .willReturn(List.of(user));

        List<User> result = adminService.findAll();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_shouldUpdateUser_whenVpaExists() {

        given(vpaRepository.findByVpaId("valid@upi"))
                .willReturn(Optional.of(vpa));

        given(userRepository.save(user))
                .willReturn(user);

        User updated = adminService.updateUser("valid@upi", AppStatus.ACTIVE);

        assertEquals(AppStatus.ACTIVE, updated.getAppStatus());

        verify(vpaRepository).findByVpaId("valid@upi");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowException_whenVpaNotFound() {

        given(vpaRepository.findByVpaId("invalid@upi"))
                .willReturn(Optional.empty());

        assertThrows(VPAIdNotFoundException.class,
                () -> adminService.updateUser("invalid@upi", AppStatus.ACTIVE));

        verify(vpaRepository).findByVpaId("invalid@upi");
        verify(userRepository, never()).save(any());
    }
}
