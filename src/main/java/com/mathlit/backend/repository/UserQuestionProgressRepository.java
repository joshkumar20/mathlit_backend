package com.mathlit.backend.repository;

import com.mathlit.backend.model.UserQuestionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserQuestionProgressRepository extends JpaRepository<UserQuestionProgress, Long> {

    Optional<UserQuestionProgress> findByFirebaseUidAndQuestionId(String firebaseUid, Long questionId);

    // All favorited question IDs for a user (used by FAVORITES game mode)
    @Query("SELECT p.questionId FROM UserQuestionProgress p WHERE p.firebaseUid = :uid AND p.isFavorited = true")
    List<Long> findFavoriteQuestionIds(@Param("uid") String uid);

    // Fetch progress rows for a specific set of questions (used during progress sync)
    @Query("SELECT p FROM UserQuestionProgress p WHERE p.firebaseUid = :uid AND p.questionId IN :questionIds")
    List<UserQuestionProgress> findByFirebaseUidAndQuestionIdIn(
            @Param("uid") String uid,
            @Param("questionIds") List<Long> questionIds);
}
