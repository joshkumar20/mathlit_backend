package com.mathlit.backend.repository;

import com.mathlit.backend.model.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // ── Basic fetch ───────────────────────────────────────────────────────────

    List<Question> findBySectionAndCategory(String section, String category);

    List<Question> findBySectionAndCategoryAndDifficulty(String section, String category, String difficulty);

    // ── With exclude list (already-attempted questions) ───────────────────────

    @Query("SELECT q FROM Question q WHERE q.section = :section AND q.category = :category AND q.id NOT IN :excludeIds")
    List<Question> findBySectionAndCategoryExcluding(
            @Param("section") String section,
            @Param("category") String category,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.section = :section AND q.category = :category AND q.difficulty = :difficulty AND q.id NOT IN :excludeIds")
    List<Question> findBySectionAndCategoryAndDifficultyExcluding(
            @Param("section") String section,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    // ── Counts (for totalAvailable in response) ───────────────────────────────

    long countBySectionAndCategory(String section, String category);

    long countBySectionAndCategoryAndDifficulty(String section, String category, String difficulty);

    // ── Fetch by IDs (for reattempt questions) ───────────────────────────────

    List<Question> findByIdIn(List<Long> ids);
}