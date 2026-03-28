package com.mathlit.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionFetchRequest {

    private String section;    // ARITHMETIC / ALGEBRA / GEOMETRY / TRIGONOMETRY / STATISTICS
    private String category;   // SIMPLIFICATION / RATIO / AREA / MEAN etc.
    private int count;         // number of questions requested
    private String difficulty; // ALL / EASY / MEDIUM / HARD  (default: ALL)
    private String gameMode;   // PRACTICE / SPEED_ROUND / SURVIVAL / TIMED
}