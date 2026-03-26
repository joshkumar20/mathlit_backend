package com.mathlit.backend.repository;

import com.mathlit.backend.model.GameSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByFirebaseUidOrderByPlayedAtDesc(String firebaseUid, Pageable pageable);

    @Query("SELECT gs.firebaseUid, SUM(gs.score) as weeklyScore FROM GameSession gs " +
           "WHERE gs.playedAt >= :startOfWeek GROUP BY gs.firebaseUid ORDER BY weeklyScore DESC")
    List<Object[]> findWeeklyScores(LocalDateTime startOfWeek, Pageable pageable);
}
