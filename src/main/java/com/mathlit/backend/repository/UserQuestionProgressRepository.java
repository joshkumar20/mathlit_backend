package com.mathlit.backend.repository;

import com.mathlit.backend.model.UserQuestionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserQuestionProgressRepository extends JpaRepository<UserQuestionProgress, Long> {

    Optional<UserQuestionProgress> findByFirebaseUidAndQuestionId(String firebaseUid, Long questionId);

    // All attempted question IDs for a user (to exclude from next game)
    @Query("SELECT p.questionId FROM UserQuestionProgress p WHERE p.firebaseUid = :uid AND p.isAttempted = true")
    List<Long> findAttemptedQuestionIds(@Param("uid") String uid);

    // All reattempt question IDs for a user (to include even if attempted)
    @Query("SELECT p.questionId FROM UserQuestionProgress p WHERE p.firebaseUid = :uid AND p.isMarkedReattempt = true")
    List<Long> findReattemptQuestionIds(@Param("uid") String uid);
}