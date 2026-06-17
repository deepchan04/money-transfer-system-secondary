package com.training.mts.service;

import com.training.mts.enums.TransactionStatus;
import com.training.mts.model.ScratchCard;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.repository.ScratchCardRepository;
import com.training.mts.repository.TransactionRepository;
import com.training.mts.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class RewardServiceImpl implements RewardService {

    private final ScratchCardRepository scratchCardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final Random random = new Random();

    public static class CouponTemplate {
        String title;
        String description;
        String brandPrefix;

        public CouponTemplate(String title, String description, String brandPrefix) {
            this.title = title;
            this.description = description;
            this.brandPrefix = brandPrefix;
        }
    }

    private final List<CouponTemplate> couponTemplates = List.of(
        new CouponTemplate("Flat ₹50 Off on Zomato", "Get flat ₹50 off on orders above ₹199 on Zomato", "ZOMATO"),
        new CouponTemplate("10% Off on Myntra", "Get 10% off up to ₹150 on your fashion purchases on Myntra", "MYNTRA"),
        new CouponTemplate("₹100 Swiggy Voucher", "Get ₹100 discount on Swiggy Instamart orders above ₹499", "SWIGGY"),
        new CouponTemplate("50% Off on Uber", "Get 50% discount up to ₹75 on your next 3 Uber rides", "UBER"),
        new CouponTemplate("Buy 1 Get 1 Free on BookMyShow", "Buy one movie ticket and get the second one free on BookMyShow", "BMS"),
        new CouponTemplate("3 Months Spotify Premium", "Get 3 months of free Spotify Premium subscription (new users only)", "SPOTIFY"),
        new CouponTemplate("Flat ₹100 Off on Ajio", "Get flat ₹100 off on fashion apparel on Ajio", "AJIO"),
        new CouponTemplate("15% Off on Pharmeasy", "Get 15% discount on medicines on Pharmeasy", "PHARMEASY")
    );

    public RewardServiceImpl(ScratchCardRepository scratchCardRepository,
                             UserRepository userRepository,
                             TransactionRepository transactionRepository) {
        this.scratchCardRepository = scratchCardRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void awardPointsForTransaction(Transaction transaction) {
        if (transaction == null || transaction.getStatus() != TransactionStatus.SUCCESS) {
            return;
        }

        User payer = transaction.getPayer();
        User payee = transaction.getPayee();
        Double amount = transaction.getAmount();

        // Guardrails
        if (amount == null || amount <= 100) {
            return;
        }
        if (payer == null || payee == null || payer.getId().equals(payee.getId())) {
            return;
        }

        // Payee Guardrail: Max 1 rewarded transaction per payee per calendar day
        LocalDateTime midnight = LocalDate.now().atStartOfDay();
        long successfulTransfersToday = transactionRepository.countByPayerAndPayeeAndStatusAndTransactionTimeAfter(
            payer, payee, TransactionStatus.SUCCESS, midnight
        );

        // Since the current transaction is already saved in database, count will be >= 1.
        // If count is exactly 1, this is the first transfer between them today.
        if (successfulTransfersToday > 1) {
            return;
        }

        // Calculate points (Capped at 20 points per transaction)
        int points = Math.min(20, (int) (amount / 100));
        if (points <= 0) {
            return;
        }

        // Fetch up-to-date user instance
        User user = userRepository.findById(payer.getId()).orElse(payer);

        int oldTotal = user.getTotalPointsEarned() != null ? user.getTotalPointsEarned() : 0;
        int newTotal = oldTotal + points;

        user.setRewardPoints(user.getRewardPoints() + points);
        user.setTotalPointsEarned(newTotal);
        userRepository.save(user);

        transaction.setPoints(points);
        transactionRepository.save(transaction);

        // Milestone trigger: 1 scratchcard per 10 points
        int cardsToGenerate = (newTotal / 10) - (oldTotal / 10);
        for (int i = 0; i < cardsToGenerate; i++) {
            generateScratchCard(user);
        }
    }

    @Override
    public List<ScratchCard> getUserScratchCards(User user) {
        return scratchCardRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional
    public ScratchCard scratchCard(Long cardId, User user) {
        ScratchCard card = scratchCardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new IllegalArgumentException("Scratch card not found or access denied"));

        if (card.isScratched()) {
            throw new IllegalStateException("Scratch card has already been scratched");
        }

        card.setScratched(true);
        card.setScratchedAt(LocalDateTime.now());
        return scratchCardRepository.save(card);
    }

    private void generateScratchCard(User user) {
        CouponTemplate template = couponTemplates.get(random.nextInt(couponTemplates.size()));
        
        ScratchCard card = new ScratchCard();
        card.setUser(user);
        card.setScratched(false);
        card.setTitle(template.title);
        card.setDescription(template.description);
        card.setCouponCode(generateRandomCode(template.brandPrefix));
        card.setCreatedAt(LocalDateTime.now());
        scratchCardRepository.save(card);
    }

    private String generateRandomCode(String brandPrefix) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return brandPrefix + "-" + sb.toString();
    }
}
