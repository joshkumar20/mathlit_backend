package com.mathlit.backend.service;

import com.mathlit.backend.dto.StreakStatusDto;
import com.mathlit.backend.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StreakService {

    public StreakStatusDto updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastPlayed = user.getLastPlayedDate();
        boolean extended = false;
        boolean broken = false;

        if (lastPlayed == null) {
            user.setCurrentStreak(1);
            extended = true;
        } else if (lastPlayed.equals(today)) {
            // Already played today — no change
        } else if (lastPlayed.equals(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
            extended = true;
        } else {
            user.setCurrentStreak(1);
            broken = true;
        }

        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }
        user.setLastPlayedDate(today);

        return new StreakStatusDto(
                user.getCurrentStreak(),
                user.getLongestStreak(),
                today,
                broken,
                extended
        );
    }
}
