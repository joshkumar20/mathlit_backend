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

    // ── Exclude already-attempted (NOT EXISTS — avoids large IN lists) ─────────
    //
    // NOT EXISTS uses the (firebase_uid, question_id) unique index on
    // user_question_progress, so it stays fast even when a user has attempted
    // thousands of questions. The Java side never loads the ID list at all.

    @Query("""
            SELECT q FROM Question q
            WHERE LOWER(q.section) = LOWER(:section)
              AND LOWER(q.category) = LOWER(:category)
              AND NOT EXISTS (
                  SELECT 1 FROM UserQuestionProgress p
                  WHERE p.questionId = q.id
                    AND p.firebaseUid = :uid
                    AND p.isAttempted = true
              )
            """)
    List<Question> findUnattempted(
            @Param("section") String section,
            @Param("category") String category,
            @Param("uid") String uid,
            Pageable pageable);

    @Query("""
            SELECT q FROM Question q
            WHERE LOWER(q.section) = LOWER(:section)
              AND LOWER(q.category) = LOWER(:category)
              AND LOWER(q.difficulty) = LOWER(:difficulty)
              AND NOT EXISTS (
                  SELECT 1 FROM UserQuestionProgress p
                  WHERE p.questionId = q.id
                    AND p.firebaseUid = :uid
                    AND p.isAttempted = true
              )
            """)
    List<Question> findUnattemptedByDifficulty(
            @Param("section") String section,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("uid") String uid,
            Pageable pageable);

    // ── Counts (for totalAvailable in response) ───────────────────────────────

    @Query("SELECT COUNT(q) FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category)")
    long countBySectionAndCategory(@Param("section") String section, @Param("category") String category);

    @Query("SELECT COUNT(q) FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.difficulty) = LOWER(:difficulty)")
    long countBySectionAndCategoryAndDifficulty(@Param("section") String section, @Param("category") String category, @Param("difficulty") String difficulty);

    // ── Fetch by IDs (for favorites) ──────────────────────────────────────────

    List<Question> findByIdIn(List<Long> ids);
}
