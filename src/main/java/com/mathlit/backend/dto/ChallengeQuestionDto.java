package com.mathlit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeQuestionDto {
    private int questionNumber;
    private String questionText;  // e.g. "12 + 7"
    private String answer;        // e.g. "19"
}