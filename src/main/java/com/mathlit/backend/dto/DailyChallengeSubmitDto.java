package com.mathlit.backend.dto;

import lombok.Data;

@Data
public class DailyChallengeSubmitDto {
    private int score;
    private int correctAnswers;
    private int totalQuestions;
}
