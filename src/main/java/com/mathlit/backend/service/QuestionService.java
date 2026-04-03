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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserQuestionProgressRepository progressRepository;

    // ── Fetch Questions ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public QuestionFetchResponse fetchQuestions(String firebaseUid, QuestionFetchRequest request) {
        String section    = request.getSection().toUpperCase();
        String category   = request.getCategory().toUpperCase();
        String difficulty = request.getDifficulty() == null ? "ALL" : request.getDifficulty().toUpperCase();
        int count         = request.getCount();
        String gameMode   = request.getGameMode() == null ? "PRACTICE" : request.getGameMode().toUpperCase();

        // Competitive extra filters
        String tag        = request.getTag() != null ? request.getTag().toUpperCase() : null;
        String examSource = request.getExamSource() != null ? request.getExamSource().toUpperCase() : null;
        boolean isCompetitive = tag != null && examSource != null;

        // ── FAVORITES mode: only return questions the user has favorited ──────
        if ("FAVORITES".equals(gameMode)) {
            List<Long> favoriteIds = progressRepository.findFavoriteQuestionIds(firebaseUid);
            if (favoriteIds.isEmpty()) {
                return new QuestionFetchResponse(section, category, 0, 0, Collections.emptyList());
            }

            List<Question> favorites = questionRepository.findByIdIn(favoriteIds).stream()
                    .filter(q -> q.getSection().equalsIgnoreCase(section)
                              && q.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());

            if ("ALL".equals(difficulty)) {
                // no filter
            } else {
                favorites = favorites.stream()
                        .filter(q -> q.getDifficulty().equalsIgnoreCase(difficulty))
                        .collect(Collectors.toList());
            }

            int totalFavorites = favorites.size();
            Collections.shuffle(favorites);
            if (favorites.size() > count) favorites = favorites.subList(0, count);

            Set<Long> favIdSet = new HashSet<>(favoriteIds);
            List<QuestionDto> dtos = favorites.stream()
                    .map(q -> toDto(q, favIdSet))
                    .collect(Collectors.toList());

            return new QuestionFetchResponse(section, category, totalFavorites, dtos.size(), dtos);
        }

        // ── Normal and Competitive modes: exclude already-attempted, reset when all done ──

        List<Long> favoriteIds = progressRepository.findFavoriteQuestionIds(firebaseUid);
        Set<Long> favIdSet     = new HashSet<>(favoriteIds);

        List<Question> fresh;
        long totalAvailable;

        if (isCompetitive) {
            totalAvailable = questionRepository.countBySectionAndCategoryAndTagAndExamSource(
                    section, category, tag, examSource);
            fresh = fetchUnattemptedCompetitive(section, category, tag, examSource, difficulty, firebaseUid, count * 2);
            if (fresh.size() < count) {
                fresh = fetchFromDbCompetitive(section, category, tag, examSource, difficulty, count);
            }
        } else {
            totalAvailable = "ALL".equals(difficulty)
                    ? questionRepository.countBySectionAndCategory(section, category)
                    : questionRepository.countBySectionAndCategoryAndDifficulty(section, category, difficulty);
            // NOT EXISTS subquery — no Java-side ID list needed
            fresh = fetchUnattempted(section, category, difficulty, firebaseUid, count * 2);
            if (fresh.size() < count) {
                fresh = fetchFromDb(section, category, difficulty, count);
            }
        }

        Collections.shuffle(fresh);
        if (fresh.size() > count) fresh = fresh.subList(0, count);

        List<QuestionDto> dtos = fresh.stream()
                .map(q -> toDto(q, favIdSet))
                .collect(Collectors.toList());

        return new QuestionFetchResponse(section, category, (int) totalAvailable, dtos.size(), dtos);
    }

    // Fetch unattempted questions using NOT EXISTS (no Java-side ID list)
    private List<Question> fetchUnattempted(String section, String category,
                                             String difficulty, String uid, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        return "ALL".equals(difficulty)
                ? questionRepository.findUnattempted(section, category, uid, page)
                : questionRepository.findUnattemptedByDifficulty(section, category, difficulty, uid, page);
    }

    // Full pool fetch (used when all questions exhausted — reset)
    private List<Question> fetchFromDb(String section, String category,
                                        String difficulty, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        return "ALL".equals(difficulty)
                ? questionRepository.findBySectionAndCategory(section, category)
                : questionRepository.findBySectionAndCategoryAndDifficulty(section, category, difficulty);
    }

    // Competitive — unattempted fetch
    private List<Question> fetchUnattemptedCompetitive(String section, String category,
                                                        String tag, String examSource,
                                                        String difficulty, String uid, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        return "ALL".equals(difficulty)
                ? questionRepository.findUnattemptedCompetitive(section, category, tag, examSource, uid, page)
                : questionRepository.findUnattemptedCompetitiveByDifficulty(section, category, tag, examSource, difficulty, uid, page);
    }

    // Competitive — full pool fetch (reset)
    private List<Question> fetchFromDbCompetitive(String section, String category,
                                                   String tag, String examSource,
                                                   String difficulty, int limit) {
        return "ALL".equals(difficulty)
                ? questionRepository.findBySectionAndCategoryAndTagAndExamSource(section, category, tag, examSource)
                : questionRepository.findBySectionAndCategoryAndTagAndExamSourceAndDifficulty(section, category, tag, examSource, difficulty);
    }

    // ── Mark Progress (called after game ends) ────────────────────────────────

    @Transactional
    public void markProgress(String firebaseUid, MarkProgressRequest request) {
        List<Long> sessionIds   = orEmpty(request.getSessionQuestionIds());
        List<Long> attemptedIds = orEmpty(request.getAttemptedQuestionIds());
        List<Long> favoriteIds  = orEmpty(request.getFavoriteQuestionIds());

        Set<Long> favSet = new HashSet<>(favoriteIds);

        // Bulk-load existing progress rows for this session to avoid N+1
        Map<Long, UserQuestionProgress> existing = progressRepository
                .findByFirebaseUidAndQuestionIdIn(firebaseUid, sessionIds)
                .stream()
                .collect(Collectors.toMap(UserQuestionProgress::getQuestionId, p -> p));

        List<UserQuestionProgress> toSave = new ArrayList<>();

        for (Long questionId : sessionIds) {
            UserQuestionProgress p = existing.getOrDefault(questionId, newProgress(firebaseUid, questionId));

            if (attemptedIds.contains(questionId)) {
                p.setAttempted(true);
                p.setLastAttemptedAt(LocalDateTime.now());
            }

            p.setFavorited(favSet.contains(questionId));
            toSave.add(p);
        }

        progressRepository.saveAll(toSave);
    }

    private UserQuestionProgress newProgress(String uid, Long questionId) {
        UserQuestionProgress p = new UserQuestionProgress();
        p.setFirebaseUid(uid);
        p.setQuestionId(questionId);
        return p;
    }

    private List<Long> orEmpty(List<Long> list) {
        return list != null ? list : Collections.emptyList();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private QuestionDto toDto(Question q, Set<Long> favoriteIds) {
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
        dto.setQuestionImageUrl(q.getQuestionImageUrl());
        dto.setSolutionImageUrl(q.getSolutionImageUrl());
        dto.setFavorited(favoriteIds.contains(q.getId()));
        return dto;
    }
}
