package com.mathlit.backend.service;

import com.mathlit.backend.dto.MarkProgressRequest;
import com.mathlit.backend.dto.QuestionDto;
import com.mathlit.backend.dto.QuestionFetchRequest;
import com.mathlit.backend.dto.QuestionFetchResponse;
import com.mathlit.backend.model.Question;
import com.mathlit.backend.model.UserQuestionProgress;
import com.mathlit.backend.repository.QuestionRepository;
import com.mathlit.backend.repository.UserQuestionProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserQuestionProgressRepository progressRepository;

    // ── Fetch Questions ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public QuestionFetchResponse fetchQuestions(String firebaseUid, QuestionFetchRequest request) {
        String section    = request.getSection();
        String category   = request.getCategory();
        String difficulty = request.getDifficulty() == null ? "ALL" : request.getDifficulty();
        int count         = request.getCount();

        // Total questions available in DB for this category
        long totalAvailable = "ALL".equals(difficulty)
                ? questionRepository.countBySectionAndCategory(section, category)
                : questionRepository.countBySectionAndCategoryAndDifficulty(section, category, difficulty);

        // IDs the user already attempted
        List<Long> attemptedIds = progressRepository.findAttemptedQuestionIds(firebaseUid);

        // IDs marked for reattempt — these override attempted status (appear again)
        List<Long> reattemptIds = progressRepository.findReattemptQuestionIds(firebaseUid);

        // Final exclude list = attempted - reattempt (reattempt ones should still appear)
        List<Long> excludeIds = attemptedIds.stream()
                .filter(id -> !reattemptIds.contains(id))
                .collect(Collectors.toList());

        // Fetch unattempted questions
        List<Question> fresh = fetchFromDb(section, category, difficulty, excludeIds, count * 2);

        // Fetch reattempt questions separately and merge at the front
        List<Question> reattemptQuestions = new ArrayList<>();
        if (!reattemptIds.isEmpty()) {
            reattemptQuestions = questionRepository.findByIdIn(reattemptIds);
            // Filter by section/category in case reattempt spans other categories
            reattemptQuestions = reattemptQuestions.stream()
                    .filter(q -> q.getSection().equals(section) && q.getCategory().equals(category))
                    .collect(Collectors.toList());
        }

        // Merge: reattempt questions first, then fresh ones, shuffle each group
        Collections.shuffle(reattemptQuestions);
        Collections.shuffle(fresh);

        List<Question> merged = new ArrayList<>();
        merged.addAll(reattemptQuestions);
        merged.addAll(fresh);

        // If still not enough (all questions exhausted), reset and refetch all
        if (merged.isEmpty()) {
            merged = fetchFromDb(section, category, difficulty, Collections.emptyList(), count);
            Collections.shuffle(merged);
        }

        // Limit to requested count
        if (merged.size() > count) {
            merged = merged.subList(0, count);
        }

        List<QuestionDto> dtos = merged.stream().map(this::toDto).collect(Collectors.toList());

        return new QuestionFetchResponse(section, category, (int) totalAvailable, dtos.size(), dtos);
    }

    private List<Question> fetchFromDb(String section, String category, String difficulty,
                                        List<Long> excludeIds, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        if (excludeIds.isEmpty()) {
            return "ALL".equals(difficulty)
                    ? questionRepository.findBySectionAndCategory(section, category)
                    : questionRepository.findBySectionAndCategoryAndDifficulty(section, category, difficulty);
        } else {
            return "ALL".equals(difficulty)
                    ? questionRepository.findBySectionAndCategoryExcluding(section, category, excludeIds, page)
                    : questionRepository.findBySectionAndCategoryAndDifficultyExcluding(section, category, difficulty, excludeIds, page);
        }
    }

    // ── Mark Progress (called after game ends) ────────────────────────────────

    @Transactional
    public void markProgress(String firebaseUid, MarkProgressRequest request) {
        // Mark attempted questions
        if (request.getAttemptedQuestionIds() != null) {
            for (Long questionId : request.getAttemptedQuestionIds()) {
                UserQuestionProgress progress = getOrCreate(firebaseUid, questionId);
                progress.setAttempted(true);
                progress.setMarkedReattempt(false); // clear reattempt flag once re-attempted
                progress.setLastAttemptedAt(LocalDateTime.now());
                progressRepository.save(progress);
            }
        }

        // Mark reattempt questions — set is_attempted = false so they reappear next game
        if (request.getReattemptQuestionIds() != null) {
            for (Long questionId : request.getReattemptQuestionIds()) {
                UserQuestionProgress progress = getOrCreate(firebaseUid, questionId);
                progress.setMarkedReattempt(true);
                progress.setAttempted(false); // reset so it shows up again
                progressRepository.save(progress);
            }
        }
    }

    private UserQuestionProgress getOrCreate(String firebaseUid, Long questionId) {
        Optional<UserQuestionProgress> existing =
                progressRepository.findByFirebaseUidAndQuestionId(firebaseUid, questionId);
        if (existing.isPresent()) {
            return existing.get();
        }
        UserQuestionProgress progress = new UserQuestionProgress();
        progress.setFirebaseUid(firebaseUid);
        progress.setQuestionId(questionId);
        return progress;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private QuestionDto toDto(Question q) {
        QuestionDto dto = new QuestionDto();
        dto.setId(q.getId());
        dto.setQuestionNo(q.getQuestionNo());
        dto.setDifficulty(q.getDifficulty());
        dto.setQuestionText(q.getQuestionText());
        dto.setOptionA(q.getOptionA());
        dto.setOptionB(q.getOptionB());
        dto.setOptionC(q.getOptionC());
        dto.setOptionD(q.getOptionD());
        dto.setCorrectIndex(q.getCorrectIndex());
        dto.setSolution(q.getSolution());
        dto.setImageUrl(q.getImageUrl());
        return dto;
    }
}