package com.mathlit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class StreakStatusDto {
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastPlayedDate;
    private boolean streakBroken;
    private boolean streakExtended;
}
