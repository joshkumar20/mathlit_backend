package com.mathlit.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MarkProgressRequest {

    // Question IDs that were attempted in this game session
    private List<Long> attemptedQuestionIds;

    // Question IDs that user marked for reattempt
    // These will have is_attempted reset to false so they reappear next game
    private List<Long> reattemptQuestionIds;
}