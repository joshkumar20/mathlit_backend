package com.mathlit.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MarkProgressRequest {

    // All question IDs shown in this game session
    // Used to know which questions' favorite state to update
    private List<Long> sessionQuestionIds;

    // Question IDs that were answered in this session
    private List<Long> attemptedQuestionIds;

    // Subset of sessionQuestionIds that user has favorited (current state)
    // For session questions NOT in this list → is_favorited set to false
    private List<Long> favoriteQuestionIds;
}
