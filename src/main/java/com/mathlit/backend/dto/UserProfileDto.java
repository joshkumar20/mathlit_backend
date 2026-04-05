package com.mathlit.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDto {
    private String uid;
    private String displayName;
    private String email;
    private String avatarUrl;
    private int xp;
    private int level;
    private int currentStreak;
    private int longestStreak;
    private int totalGames;
    private int totalScore;
    private int totalCorrect;
    private int totalAttempted;
    private double accuracy;
    private int highestScore;
    private boolean isNewUser;
    private String lastPlayedDate;
}
