package com.mathlit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameSessionResponse {
    private Long sessionId;
    private int xpEarned;
    private int newLevel;
    private boolean leveledUp;
    private int newStreak;
}
