package com.mathlit.backend.scheduler;

import com.mathlit.backend.service.DailyChallengeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyChallengeScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyChallengeScheduler.class);
    private final DailyChallengeService dailyChallengeService;

    public DailyChallengeScheduler(DailyChallengeService dailyChallengeService) {
        this.dailyChallengeService = dailyChallengeService;
    }

    // Runs at 12:01 AM every day
    @Scheduled(cron = "0 1 0 * * *")
    public void generateDailyChallenge() {
        LocalDate today = LocalDate.now();
        if (dailyChallengeService.questionsExistForDate(today)) {
            log.info("Daily challenge already exists for {}, skipping", today);
            return;
        }
        log.info("Generating daily challenge for {}", today);
        dailyChallengeService.generateChallenge(today);
        log.info("Daily challenge generated for {}", today);
    }
}
