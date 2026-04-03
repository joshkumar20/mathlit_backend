package com.mathlit.backend.repository;

import com.mathlit.backend.model.Question;
import org.springframework.data.domain.Page;
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

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND NOT EXISTS (SELECT p FROM UserQuestionProgress p WHERE p.questionId = q.id AND p.firebaseUid = :uid AND p.isAttempted = true)")
    List<Question> findUnattempted(
            @Param("section") String section,
            @Param("category") String category,
            @Param("uid") String uid,
            Pageable pageable);

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.difficulty) = LOWER(:difficulty) AND NOT EXISTS (SELECT p FROM UserQuestionProgress p WHERE p.questionId = q.id AND p.firebaseUid = :uid AND p.isAttempted = true)")
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

    // ── Admin: paginated list with optional filters ───────────────────────────

    @Query("SELECT q FROM Question q WHERE " +
           "(:section IS NULL OR LOWER(q.section) = LOWER(:section)) AND " +
           "(:category IS NULL OR LOWER(q.category) = LOWER(:category)) AND " +
           "(:difficulty IS NULL OR LOWER(q.difficulty) = LOWER(:difficulty)) AND " +
           "(:search IS NULL OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Question> findWithFilters(@Param("section") String section,
                                   @Param("category") String category,
                                   @Param("difficulty") String difficulty,
                                   @Param("search") String search,
                                   Pageable pageable);

    // ── Admin: count per section for dashboard stats ──────────────────────────

    @Query("SELECT q.section, COUNT(q) FROM Question q GROUP BY q.section")
    List<Object[]> countBySection();

    // ── Competitive: section + category + tag + examSource ────────────────────

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.tag) = LOWER(:tag) AND LOWER(q.examSource) = LOWER(:examSource)")
    List<Question> findBySectionAndCategoryAndTagAndExamSource(
            @Param("section") String section, @Param("category") String category,
            @Param("tag") String tag, @Param("examSource") String examSource);

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.tag) = LOWER(:tag) AND LOWER(q.examSource) = LOWER(:examSource) AND LOWER(q.difficulty) = LOWER(:difficulty)")
    List<Question> findBySectionAndCategoryAndTagAndExamSourceAndDifficulty(
            @Param("section") String section, @Param("category") String category,
            @Param("tag") String tag, @Param("examSource") String examSource,
            @Param("difficulty") String difficulty);

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.tag) = LOWER(:tag) AND LOWER(q.examSource) = LOWER(:examSource) AND NOT EXISTS (SELECT p FROM UserQuestionProgress p WHERE p.questionId = q.id AND p.firebaseUid = :uid AND p.isAttempted = true)")
    List<Question> findUnattemptedCompetitive(
            @Param("section") String section, @Param("category") String category,
            @Param("tag") String tag, @Param("examSource") String examSource,
            @Param("uid") String uid, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.tag) = LOWER(:tag) AND LOWER(q.examSource) = LOWER(:examSource) AND LOWER(q.difficulty) = LOWER(:difficulty) AND NOT EXISTS (SELECT p FROM UserQuestionProgress p WHERE p.questionId = q.id AND p.firebaseUid = :uid AND p.isAttempted = true)")
    List<Question> findUnattemptedCompetitiveByDifficulty(
            @Param("section") String section, @Param("category") String category,
            @Param("tag") String tag, @Param("examSource") String examSource,
            @Param("difficulty") String difficulty, @Param("uid") String uid, Pageable pageable);

    @Query("SELECT COUNT(q) FROM Question q WHERE LOWER(q.section) = LOWER(:section) AND LOWER(q.category) = LOWER(:category) AND LOWER(q.tag) = LOWER(:tag) AND LOWER(q.examSource) = LOWER(:examSource)")
    long countBySectionAndCategoryAndTagAndExamSource(
            @Param("section") String section, @Param("category") String category,
            @Param("tag") String tag, @Param("examSource") String examSource);
}
