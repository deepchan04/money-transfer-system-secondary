package com.training.mts.service;

import com.training.mts.enums.TransactionStatus;
import com.training.mts.model.ScratchCard;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.repository.ScratchCardRepository;
import com.training.mts.repository.TransactionRepository;
import com.training.mts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private ScratchCardRepository scratchCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;

    private User payer;
    private User payee;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        payer = new User();
        payer.setId(1L);
        payer.setRewardPoints(0);
        payer.setTotalPointsEarned(0);

        payee = new User();
        payee.setId(2L);
        payee.setRewardPoints(0);
        payee.setTotalPointsEarned(0);

        transaction = new Transaction();
        transaction.setPayer(payer);
        transaction.setPayee(payee);
        transaction.setAmount(250.0);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setTransactionTime(LocalDateTime.now());
    }

    @Test
    void awardPointsForTransaction_success_not_capped() {
        // Rs 250 gets 2 points
        when(transactionRepository.countByPayerAndPayeeAndStatusAndTransactionTimeAfter(
                eq(payer), eq(payee), eq(TransactionStatus.SUCCESS), any(LocalDateTime.class)))
                .thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));

        rewardService.awardPointsForTransaction(transaction);

        assertEquals(2, payer.getRewardPoints());
        assertEquals(2, payer.getTotalPointsEarned());
        verify(userRepository).save(payer);
        verify(scratchCardRepository, never()).save(any(ScratchCard.class));
    }

    @Test
    void awardPointsForTransaction_success_capped_and_milestone() {
        // Rs 2500 gets 20 points (capped)
        transaction.setAmount(2500.0);

        when(transactionRepository.countByPayerAndPayeeAndStatusAndTransactionTimeAfter(
                eq(payer), eq(payee), eq(TransactionStatus.SUCCESS), any(LocalDateTime.class)))
                .thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));

        rewardService.awardPointsForTransaction(transaction);

        assertEquals(20, payer.getRewardPoints());
        assertEquals(20, payer.getTotalPointsEarned());
        verify(userRepository).save(payer);
        // Milestone at 10 and 20: generates 2 scratchcards
        verify(scratchCardRepository, times(2)).save(any(ScratchCard.class));
    }

    @Test
    void awardPointsForTransaction_self_transfer_ignored() {
        transaction.setPayee(payer); // self transfer

        rewardService.awardPointsForTransaction(transaction);

        assertEquals(0, payer.getRewardPoints());
        verifyNoInteractions(userRepository);
    }

    @Test
    void awardPointsForTransaction_amount_too_low() {
        transaction.setAmount(100.0); // not > 100

        rewardService.awardPointsForTransaction(transaction);

        assertEquals(0, payer.getRewardPoints());
        verifyNoInteractions(userRepository);
    }

    @Test
    void awardPointsForTransaction_payee_guardrail_prevents_rewards() {
        // Multiple successful transactions to same payee today
        when(transactionRepository.countByPayerAndPayeeAndStatusAndTransactionTimeAfter(
                eq(payer), eq(payee), eq(TransactionStatus.SUCCESS), any(LocalDateTime.class)))
                .thenReturn(2L); // 2 transactions today

        rewardService.awardPointsForTransaction(transaction);

        assertEquals(0, payer.getRewardPoints());
        verifyNoInteractions(userRepository);
    }

    @Test
    void scratchCard_success() {
        ScratchCard card = new ScratchCard();
        card.setId(10L);
        card.setUser(payer);
        card.setScratched(false);
        card.setCouponCode("ZOMATO-TEST");

        when(scratchCardRepository.findByIdAndUser(10L, payer)).thenReturn(Optional.of(card));
        when(scratchCardRepository.save(card)).thenReturn(card);

        ScratchCard result = rewardService.scratchCard(10L, payer);

        assertTrue(result.isScratched());
        assertNotNull(result.getScratchedAt());
        verify(scratchCardRepository).save(card);
    }

    @Test
    void scratchCard_already_scratched_throws_exception() {
        ScratchCard card = new ScratchCard();
        card.setId(10L);
        card.setUser(payer);
        card.setScratched(true);

        when(scratchCardRepository.findByIdAndUser(10L, payer)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () -> rewardService.scratchCard(10L, payer));
        verify(scratchCardRepository, never()).save(any(ScratchCard.class));
    }
}
