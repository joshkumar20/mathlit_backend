package com.mathlit.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameSessionDto {
    private Long id;
    private String operation;
    private String gameMode;
    private int score;
    private int correctAnswers;
    private int wrongAnswers;
    private int totalQuestions;
    private double accuracy;
    private int durationSecs;
    private LocalDateTime playedAt;
}
