package com.mathlit.backend.repository;

import com.mathlit.backend.model.DailyChallengeResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyChallengeResultRepository extends JpaRepository<DailyChallengeResult, Long> {
    Optional<DailyChallengeResult> findByFirebaseUidAndChallengeDate(String firebaseUid, LocalDate date);

    List<DailyChallengeResult> findByChallengeDateOrderByScoreDesc(LocalDate date, Pageable pageable);

    List<DailyChallengeResult> findByFirebaseUidOrderByChallengeDateDesc(String firebaseUid);

    long countByChallengeDateAndScoreGreaterThan(LocalDate date, int score);

    long countByChallengeDate(LocalDate date);
}
