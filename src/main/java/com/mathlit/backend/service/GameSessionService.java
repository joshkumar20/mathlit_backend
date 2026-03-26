package com.mathlit.backend.service;

import com.mathlit.backend.dto.GameSessionDto;
import com.mathlit.backend.dto.GameSessionResponse;
import com.mathlit.backend.dto.StreakStatusDto;
import com.mathlit.backend.model.GameSession;
import com.mathlit.backend.model.User;
import com.mathlit.backend.repository.GameSessionRepository;
import com.mathlit.backend.repository.UserRepository;
import com.mathlit.backend.util.XpCalculator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public GameSessionService(GameSessionRepository gameSessionRepository,
                               UserRepository userRepository,
                               StreakService streakService) {
        this.gameSessionRepository = gameSessionRepository;
        this.userRepository = userRepository;
        this.streakService = streakService;
    }

    @Transactional
    public GameSessionResponse saveSession(String uid, GameSessionDto dto) {
        User user = userRepository.findByFirebaseUid(uid)
                .orElseThrow(() -> new RuntimeException("User not found: " + uid));

        GameSession session = new GameSession();
        session.setFirebaseUid(uid);
        session.setOperation(dto.getOperation());
        session.setGameMode(dto.getGameMode());
        session.setScore(dto.getScore());
        session.setCorrectAnswers(dto.getCorrectAnswers());
        session.setWrongAnswers(dto.getWrongAnswers());
        session.setTotalQuestions(dto.getTotalQuestions());
        session.setDurationSecs(dto.getDurationSecs());
        if (dto.getTotalQuestions() > 0) {
            session.setAccuracy((double) dto.getCorrectAnswers() / dto.getTotalQuestions() * 100);
        }
        GameSession saved = gameSessionRepository.save(session);

        int xpEarned = XpCalculator.calculateXp(dto.getCorrectAnswers(), dto.getGameMode());
        int oldLevel = user.getLevel();

        user.setTotalScore(user.getTotalScore() + dto.getScore());
        user.setTotalCorrect(user.getTotalCorrect() + dto.getCorrectAnswers());
        user.setTotalAttempted(user.getTotalAttempted() + dto.getTotalQuestions());
        user.setTotalGames(user.getTotalGames() + 1);
        user.setXp(user.getXp() + xpEarned);
        user.setLevel(XpCalculator.getLevel(user.getXp()));
        if (dto.getScore() > user.getHighestScore()) {
            user.setHighestScore(dto.getScore());
        }

        StreakStatusDto streak = streakService.updateStreak(user);
        userRepository.save(user);

        return new GameSessionResponse(
                saved.getId(),
                xpEarned,
                user.getLevel(),
                user.getLevel() > oldLevel,
                streak.getCurrentStreak()
        );
    }

    public List<GameSessionDto> getRecentSessions(String uid, int limit) {
        return gameSessionRepository
                .findByFirebaseUidOrderByPlayedAtDesc(uid, PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private GameSessionDto toDto(GameSession gs) {
        GameSessionDto dto = new GameSessionDto();
        dto.setId(gs.getId());
        dto.setOperation(gs.getOperation());
        dto.setGameMode(gs.getGameMode());
        dto.setScore(gs.getScore());
        dto.setCorrectAnswers(gs.getCorrectAnswers());
        dto.setWrongAnswers(gs.getWrongAnswers());
        dto.setTotalQuestions(gs.getTotalQuestions());
        dto.setAccuracy(gs.getAccuracy());
        dto.setDurationSecs(gs.getDurationSecs());
        dto.setPlayedAt(gs.getPlayedAt());
        return dto;
    }
}
