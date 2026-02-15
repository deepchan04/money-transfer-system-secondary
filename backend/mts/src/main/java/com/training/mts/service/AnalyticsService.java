package com.training.mts.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class AnalyticsService {

    public boolean triggerSnowflakeSync() {
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
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("SYNC_LOG: " + line);
                }
            }

            // 4. Wait for completion and return success status
            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to execute sync script: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

