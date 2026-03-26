package com.mathlit.backend.service;

import com.mathlit.backend.dto.LeaderboardEntryDto;
import com.mathlit.backend.dto.LeaderboardResponse;
import com.mathlit.backend.dto.MyRankDto;
import com.mathlit.backend.model.DailyChallengeResult;
import com.mathlit.backend.model.User;
import com.mathlit.backend.repository.DailyChallengeResultRepository;
import com.mathlit.backend.repository.GameSessionRepository;
import com.mathlit.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final DailyChallengeResultRepository dailyResultRepository;

    public LeaderboardService(UserRepository userRepository,
                               GameSessionRepository gameSessionRepository,
                               DailyChallengeResultRepository dailyResultRepository) {
        this.userRepository = userRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.dailyResultRepository = dailyResultRepository;
    }

    public LeaderboardResponse getWeeklyLeaderboard(String uid, int limit) {
        LocalDateTime startOfWeek = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        List<Object[]> rows = gameSessionRepository.findWeeklyScores(startOfWeek, PageRequest.of(0, limit));

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        AtomicLong rank = new AtomicLong(1);
        MyRankDto myRank = null;

        for (Object[] row : rows) {
            String rowUid = (String) row[0];
            long score = ((Number) row[1]).longValue();
            User user = userRepository.findByFirebaseUid(rowUid).orElse(null);
            if (user == null) continue;

            long r = rank.getAndIncrement();
            entries.add(new LeaderboardEntryDto(r, rowUid, user.getDisplayName(),
                    user.getAvatarUrl(), user.getLevel(), score));

            if (rowUid.equals(uid)) {
                myRank = new MyRankDto(r, score);
            }
        }

        return new LeaderboardResponse(LocalDateTime.now(), entries, myRank);
    }

    public LeaderboardResponse getAllTimeLeaderboard(String uid, int limit) {
        List<User> users = userRepository.findTopByTotalScore(limit);

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        AtomicLong rank = new AtomicLong(1);
        MyRankDto myRank = null;

        for (User user : users) {
            long r = rank.getAndIncrement();
            entries.add(new LeaderboardEntryDto(r, user.getFirebaseUid(), user.getDisplayName(),
                    user.getAvatarUrl(), user.getLevel(), user.getTotalScore()));
            if (user.getFirebaseUid().equals(uid)) {
                myRank = new MyRankDto(r, user.getTotalScore());
            }
        }

        return new LeaderboardResponse(LocalDateTime.now(), entries, myRank);
    }

    public LeaderboardResponse getDailyLeaderboard(String uid) {
        LocalDate today = LocalDate.now();
        List<DailyChallengeResult> results = dailyResultRepository
                .findByChallengeDateOrderByScoreDesc(today, PageRequest.of(0, 50));

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        AtomicLong rank = new AtomicLong(1);
        MyRankDto myRank = null;

        for (DailyChallengeResult result : results) {
            User user = userRepository.findByFirebaseUid(result.getFirebaseUid()).orElse(null);
            if (user == null) continue;

            long r = rank.getAndIncrement();
            entries.add(new LeaderboardEntryDto(r, result.getFirebaseUid(), user.getDisplayName(),
                    user.getAvatarUrl(), user.getLevel(), result.getScore()));
            if (result.getFirebaseUid().equals(uid)) {
                myRank = new MyRankDto(r, result.getScore());
            }
        }

        return new LeaderboardResponse(LocalDateTime.now(), entries, myRank);
    }
}
