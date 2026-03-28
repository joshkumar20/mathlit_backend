package com.mathlit.backend.repository;

import com.mathlit.backend.model.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // ── Basic fetch (case-insensitive) ────────────────────────────────────────

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category)")
    List<Question> findBySectionAndCategory(@Param("section") String section, @Param("category") String category);

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.difficulty) = LOWER(:difficulty)")
    List<Question> findBySectionAndCategoryAndDifficulty(@Param("section") String section, @Param("category") String category, @Param("difficulty") String difficulty);

    // ── With exclude list (already-attempted questions) ───────────────────────

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND q.id NOT IN :excludeIds")
    List<Question> findBySectionAndCategoryExcluding(
            @Param("section") String section,
            @Param("category") String category,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.difficulty) = LOWER(:difficulty) AND q.id NOT IN :excludeIds")
    List<Question> findBySectionAndCategoryAndDifficultyExcluding(
            @Param("section") String section,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    // ── Counts (for totalAvailable in response) ───────────────────────────────

    @Query("SELECT COUNT(q) FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category)")
    long countBySectionAndCategory(@Param("section") String section, @Param("category") String category);

    @Query("SELECT COUNT(q) FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.difficulty) = LOWER(:difficulty)")
    long countBySectionAndCategoryAndDifficulty(@Param("section") String section, @Param("category") String category, @Param("difficulty") String difficulty);

    // ── Fetch by IDs (for reattempt questions) ───────────────────────────────

    List<Question> findByIdIn(List<Long> ids);
}