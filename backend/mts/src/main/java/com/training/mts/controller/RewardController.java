package com.training.mts.controller;

import com.training.mts.dto.RewardStatusDTO;
import com.training.mts.dto.ScratchCardDTO;
import com.training.mts.model.User;
import com.training.mts.model.ScratchCard;
import com.training.mts.repository.UserRepository;
import com.training.mts.repository.ScratchCardRepository;
import com.training.mts.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/rewards")
public class RewardController {

    private final RewardService rewardService;
    private final UserRepository userRepository;
    private final ScratchCardRepository scratchCardRepository;

    public RewardController(RewardService rewardService,
                            UserRepository userRepository,
                            ScratchCardRepository scratchCardRepository) {
        this.rewardService = rewardService;
        this.userRepository = userRepository;
        this.scratchCardRepository = scratchCardRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Not authenticated");
        }
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for phone: " + phoneNumber));
    }

    @GetMapping("/status")
    public ResponseEntity<RewardStatusDTO> getRewardStatus() {
        User user = getAuthenticatedUser();
        
        long unscratched = scratchCardRepository.countByUserAndScratched(user, false);
        long scratched = scratchCardRepository.countByUserAndScratched(user, true);

        RewardStatusDTO dto = new RewardStatusDTO(
                user.getRewardPoints(),
                user.getTotalPointsEarned(),
                unscratched,
                scratched
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ScratchCardDTO>> getScratchCards() {
        User user = getAuthenticatedUser();
        List<ScratchCard> cards = rewardService.getUserScratchCards(user);
        List<ScratchCardDTO> dtos = cards.stream()
                .map(ScratchCardDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/scratch/{id}")
    public ResponseEntity<ScratchCardDTO> scratchCard(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        ScratchCard scratchedCard = rewardService.scratchCard(id, user);
        return ResponseEntity.ok(new ScratchCardDTO(scratchedCard));
    }
}
