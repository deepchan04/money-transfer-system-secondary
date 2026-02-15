package com.training.analytics.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class SnowflakeRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnowflakeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Run a query and get a list of maps (flexible)
    public List<Map<String, Object>> getPeakHours() {
        String sql = "SELECT HOUR(TO_TIMESTAMP(transaction_time)) AS tx_hour, COUNT(*) AS tx_count " +
                "FROM transaction GROUP BY tx_hour ORDER BY tx_count DESC";
        return jdbcTemplate.queryForList(sql);
    }

    // Example with parameters (safer against SQL injection)
    public Map<String, Object> getSuccessRate() {
        String sql = "SELECT ROUND(COUNT_IF(status = 0) * 100.0 / COUNT(*), 2) AS success_rate FROM transaction";
        return jdbcTemplate.queryForMap(sql);
    }

    public List<Map<String, Object>> getDailyVolume() {
        String sql = "SELECT " +
                "  TO_DATE(TO_TIMESTAMP(transaction_time)) AS tx_date, " +
                "  COUNT(*) AS total_count, " +
                "  SUM(amount::FLOAT) AS total_amount " +
                "FROM transaction " +
                "WHERE status = 0" +
                "GROUP BY tx_date " +
                "ORDER BY tx_date DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getMostActiveAccounts() {
        String sql = "SELECT account_id, COUNT(*) as activity_count " +
                "FROM ( " +
                "    SELECT payer_id AS account_id FROM transaction " +
                "    UNION ALL " +
                "    SELECT payee_id AS account_id FROM transaction " +
                ") " +
                "GROUP BY account_id " +
                "ORDER BY activity_count DESC " +
                "LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getAverageTransferAmount() {
        // We cast amount to FLOAT to perform the average calculation
        String sql = "SELECT AVG(amount::FLOAT) AS avg_transaction_value FROM transaction WHERE status = 0";
        return jdbcTemplate.queryForMap(sql);
    }
}
