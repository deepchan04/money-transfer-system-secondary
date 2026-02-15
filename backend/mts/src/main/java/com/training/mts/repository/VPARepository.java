package com.training.mts.repository;

import com.training.mts.model.VPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VPARepository extends JpaRepository<VPA, Long> {
    Optional<VPA> findByVpaId(String vpaId);
}

