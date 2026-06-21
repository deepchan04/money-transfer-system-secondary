package com.training.mts.controller;
import com.training.mts.dto.RewardStatusDTO;
import com.training.mts.dto.ScratchCardDTO;
import com.training.mts.model.ScratchCard;
import com.training.mts.model.User;
import com.training.mts.repository.ScratchCardRepository;
import com.training.mts.repository.UserRepository;
import com.training.mts.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

    @Mock
    private RewardService rewardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScratchCardRepository scratchCardRepository;

    @InjectMocks
    private RewardController rewardController;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setPhoneNumber("9999999999");
        user.setRewardPoints(100);
        user.setTotalPointsEarned(500);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "9999999999",
                        null
                )
        );
    }

    @Test
    void getRewardStatus_success() {
        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.of(user));

        when(scratchCardRepository.countByUserAndScratched(user, false))
                .thenReturn(2L);

        when(scratchCardRepository.countByUserAndScratched(user, true))
                .thenReturn(3L);

        ResponseEntity<RewardStatusDTO> response =
                rewardController.getRewardStatus();

        assertEquals(200, response.getStatusCode().value());

        RewardStatusDTO dto = response.getBody();

        assertNotNull(dto);
        assertEquals(100, dto.getRewardPoints());
        assertEquals(500, dto.getTotalPointsEarned());
    }

    @Test
    void getScratchCards_success() {
        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.of(user));

        ScratchCard card = new ScratchCard();

        when(rewardService.getUserScratchCards(user))
                .thenReturn(List.of(card));

        ResponseEntity<List<ScratchCardDTO>> response =
                rewardController.getScratchCards();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void scratchCard_success() {
        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.of(user));

        ScratchCard card = new ScratchCard();

        when(rewardService.scratchCard(1L, user))
                .thenReturn(card);

        ResponseEntity<ScratchCardDTO> response =
                rewardController.scratchCard(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(rewardService).scratchCard(1L, user);
    }

    @Test
    void getRewardStatus_noAuthentication() {

        SecurityContextHolder.clearContext();

        assertThrows(
                IllegalStateException.class,
                () -> rewardController.getRewardStatus()
        );
    }

    @Test
    void getRewardStatus_userNotFound() {

        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(
                Exception.class,
                () -> rewardController.getRewardStatus()
        );
    }
}
