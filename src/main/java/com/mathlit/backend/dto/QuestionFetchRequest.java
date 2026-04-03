package com.mathlit.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionFetchRequest {

    private String section;    // ARITHMETIC / ALGEBRA / GEOMETRY / TRIGONOMETRY / STATISTICS / COMPETITIVE
    private String category;   // SIMPLIFICATION / RATIO / SSC / BANK etc.
    private int count;         // number of questions requested
    private String difficulty; // ALL / EASY / MEDIUM / HARD  (default: ALL)
    private String gameMode;   // PRACTICE / SPEED_ROUND / SURVIVAL / TIMED

    // Optional — used for Competitive questions to narrow down further
    private String tag;        // e.g. PYQ / MOCKS (maps to question.tag)
    private String examSource; // e.g. SSC_CHSL_SHIFT_1 (maps to question.examSource)
}