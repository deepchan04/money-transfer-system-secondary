package com.training.analytics.controller;

import com.training.analytics.repository.SnowflakeRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snowflake")
public class SnowflakeController {

    private final SnowflakeRepository snowflakeRepository;

    public SnowflakeController(SnowflakeRepository snowflakeRepository) {
        this.snowflakeRepository = snowflakeRepository;
    }

    @GetMapping("/average-amount")
    public Map<String, Object> getAverageAmount() {
        return snowflakeRepository.getAverageTransferAmount();
    }
    @GetMapping("/active-accounts")
    public List<Map<String, Object>> getMostActiveAccounts() {
        return snowflakeRepository.getMostActiveAccounts();
    }
    @GetMapping("/peak-hours")
    public List<Map<String, Object>> getPeakHours() {
        return snowflakeRepository.getPeakHours();
    }
    @GetMapping("/daily-volume")
    public List<Map<String, Object>> getDailyVolume() {
        return snowflakeRepository.getDailyVolume();
    }
    @GetMapping("/success-rate")
    public Map<String, Object> successRate() {
        return snowflakeRepository.getSuccessRate();
    }
}
