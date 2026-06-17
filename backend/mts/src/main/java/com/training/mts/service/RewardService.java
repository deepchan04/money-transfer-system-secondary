package com.training.mts.service;

import com.training.mts.model.ScratchCard;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;

import java.util.List;

public interface RewardService {
    void awardPointsForTransaction(Transaction transaction);
    List<ScratchCard> getUserScratchCards(User user);
    ScratchCard scratchCard(Long cardId, User user);
}
