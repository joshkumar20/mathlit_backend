package com.mathlit.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionDto {

    private Long id;
    private Integer questionNo;
    private String difficulty;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer correctIndex; // 0=A, 1=B, 2=C, 3=D
    private String solution;
    private String questionImageUrl; // image shown alongside the question
    private String solutionImageUrl; // image shown in the solution/explanation
    private boolean isFavorited;     // whether this user has favorited this question
}