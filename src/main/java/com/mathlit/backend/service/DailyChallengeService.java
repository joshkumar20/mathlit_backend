package com.mathlit.backend.service;

import com.mathlit.backend.dto.DailyChallengeDto;
import com.mathlit.backend.dto.DailyChallengeResultDto;
import com.mathlit.backend.dto.DailyChallengeSubmitDto;
import com.mathlit.backend.model.DailyChallenge;
import com.mathlit.backend.model.DailyChallengeResult;
import com.mathlit.backend.repository.DailyChallengeRepository;
import com.mathlit.backend.repository.DailyChallengeResultRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class DailyChallengeService {

    private final DailyChallengeRepository challengeRepository;
    private final DailyChallengeResultRepository resultRepository;

    public DailyChallengeService(DailyChallengeRepository challengeRepository,
                                  DailyChallengeResultRepository resultRepository) {
        this.challengeRepository = challengeRepository;
        this.resultRepository = resultRepository;
    }

    public DailyChallengeDto getTodayChallenge(String uid) {
        LocalDate today = LocalDate.now();
        DailyChallenge challenge = challengeRepository.findByChallengeDate(today)
                .orElseGet(() -> generateChallenge(today));

        DailyChallengeDto dto = new DailyChallengeDto();
        dto.setDate(today);
        dto.setOperation(challenge.getOperation());
        dto.setQuestionCount(challenge.getQuestionCount());
        dto.setRangeMin(challenge.getRangeMin());
        dto.setRangeMax(challenge.getRangeMax());

        Optional<DailyChallengeResult> result = resultRepository.findByFirebaseUidAndChallengeDate(uid, today);
        dto.setCompleted(result.isPresent());
        result.ifPresent(r -> dto.setUserScore(r.getScore()));

        return dto;
    }

    @Transactional
    public DailyChallengeResultDto submitChallenge(String uid, DailyChallengeSubmitDto dto) {
        LocalDate today = LocalDate.now();

        if (resultRepository.findByFirebaseUidAndChallengeDate(uid, today).isPresent()) {
            throw new RuntimeException("Already submitted today's challenge");
        }

        DailyChallengeResult result = new DailyChallengeResult();
        result.setFirebaseUid(uid);
        result.setChallengeDate(today);
        result.setScore(dto.getScore());
        result.setCorrectAnswers(dto.getCorrectAnswers());
        resultRepository.save(result);

        long totalParticipants = resultRepository.countByChallengeDate(today);
        long betterThanMe = resultRepository.countByChallengeDateAndScoreGreaterThan(today, dto.getScore());
        long rank = betterThanMe + 1;
        double percentile = totalParticipants > 1
                ? (double)(totalParticipants - betterThanMe) / totalParticipants * 100
                : 100.0;

        return new DailyChallengeResultDto(rank, totalParticipants, dto.getScore(), percentile);
    }

    public boolean hasCompletedToday(String uid) {
        return resultRepository.findByFirebaseUidAndChallengeDate(uid, LocalDate.now()).isPresent();
    }

    public DailyChallenge generateChallenge(LocalDate date) {
        String[] operations = {"addition", "subtraction", "multiplication",
                               "division", "percentage", "squares", "tables"};
        int dayOfWeek = date.getDayOfWeek().getValue() - 1;
        String operation = operations[dayOfWeek % operations.length];

        DailyChallenge challenge = new DailyChallenge();
        challenge.setChallengeDate(date);
        challenge.setOperation(operation);
        challenge.setQuestionCount(20);
        challenge.setRangeMin(1);
        challenge.setRangeMax(50);
        return challengeRepository.save(challenge);
    }
}
