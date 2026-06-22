package com.training.mts.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    @Async
    public void triggerSnowflakeSync() {
        // 1. Path to your specific python script
        String scriptPath = "..\\..\\Automation\\full_sync.py";

        // 2. Build the command (cmd /c ensures the environment picks up 'python')
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "python", scriptPath);

        // Merge error and standard output so we can see Python errors in Java logs
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // 3. Read the output from the script (Log it to your console)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                reader.lines().forEach(line -> {});
            }

            // 4. Wait for completion and return success status
            process.waitFor();


        } catch (IOException | InterruptedException e) {
            if(logger.isErrorEnabled()) {
                logger.error("Failed to execute sync script: {}" , e.getMessage());
            }
            Thread.currentThread().interrupt();

        }
    }
}

