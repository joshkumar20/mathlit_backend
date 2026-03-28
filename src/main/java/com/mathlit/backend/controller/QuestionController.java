package com.mathlit.backend.controller;

import com.mathlit.backend.dto.MarkProgressRequest;
import com.mathlit.backend.dto.QuestionFetchRequest;
import com.mathlit.backend.dto.QuestionFetchResponse;
import com.mathlit.backend.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * POST /api/v1/questions/fetch
     *
     * Request body:
     * {
     *   "section": "ARITHMETIC",
     *   "category": "SIMPLIFICATION",
     *   "count": 10,
     *   "difficulty": "ALL",   // ALL / EASY / MEDIUM / HARD
     *   "gameMode": "PRACTICE"
     * }
     *
     * Response:
     * {
     *   "section": "ARITHMETIC",
     *   "category": "SIMPLIFICATION",
     *   "totalAvailable": 45,
     *   "returned": 10,
     *   "questions": [ { id, questionNo, difficulty, questionText,
     *                    optionA, optionB, optionC, optionD,
     *                    correctIndex, solution, imageUrl } ]
     * }
     *
     * - Already-attempted questions are automatically excluded
     * - Questions marked for reattempt appear first
     * - If all questions exhausted → starts fresh from full pool
     */
    @PostMapping("/fetch")
    public ResponseEntity<QuestionFetchResponse> fetchQuestions(
            HttpServletRequest request,
            @RequestBody QuestionFetchRequest fetchRequest) {

        String uid = (String) request.getAttribute("uid");
        QuestionFetchResponse response = questionService.fetchQuestions(uid, fetchRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/questions/progress
     *
     * Called after game ends to update which questions were attempted
     * and which were marked for reattempt.
     *
     * Request body:
     * {
     *   "attemptedQuestionIds": [1, 2, 3, 5, 7],
     *   "reattemptQuestionIds": [3, 7]
     * }
     */
    @PostMapping("/progress")
    public ResponseEntity<Void> markProgress(
            HttpServletRequest request,
            @RequestBody MarkProgressRequest progressRequest) {

        String uid = (String) request.getAttribute("uid");
        questionService.markProgress(uid, progressRequest);
        return ResponseEntity.ok().build();
    }
}