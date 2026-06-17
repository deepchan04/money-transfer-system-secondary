package com.training.mts.repository;

import com.training.mts.model.ScratchCard;
import com.training.mts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScratchCardRepository extends JpaRepository<ScratchCard, Long> {
    List<ScratchCard> findByUserOrderByCreatedAtDesc(User user);
    Optional<ScratchCard> findByIdAndUser(Long id, User user);
    long countByUserAndScratched(User user, boolean scratched);
}
