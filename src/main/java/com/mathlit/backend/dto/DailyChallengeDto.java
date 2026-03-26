package com.mathlit.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyChallengeDto {
    private LocalDate date;
    private String operation;
    private int questionCount;
    private int rangeMin;
    private int rangeMax;
    private boolean completed;
    private Integer userScore;
}
