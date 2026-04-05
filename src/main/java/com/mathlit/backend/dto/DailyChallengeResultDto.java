package com.mathlit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyChallengeResultDto {
    private long rank;
    private long totalParticipants;
    private int score;
    private double percentile;
    private int newStreak;
    private int longestStreak;
}
