package com.mathlit.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mathlit.backend.dto.ChallengeQuestionDto;
import com.mathlit.backend.dto.DailyChallengeDto;
import com.mathlit.backend.dto.DailyChallengeResultDto;
import com.mathlit.backend.dto.DailyChallengeSubmitDto;
import com.mathlit.backend.dto.StreakStatusDto;
import com.mathlit.backend.model.DailyChallenge;
import com.mathlit.backend.model.DailyChallengeQuestion;
import com.mathlit.backend.model.DailyChallengeResult;
import com.mathlit.backend.repository.DailyChallengeQuestionRepository;
import com.mathlit.backend.repository.DailyChallengeRepository;
import com.mathlit.backend.repository.DailyChallengeResultRepository;
import com.mathlit.backend.repository.UserRepository;

@Service
public class DailyChallengeService {

    // ─── Question range config ─────────────────────────────────────────────────
    // Change these values to control how hard the daily challenge questions are.
    private static final int ADD_MIN  = 1,  ADD_MAX  = 999;   // each operand range for addition
    private static final int SUB_MIN  = 1,  SUB_MAX  = 999;   // each operand range for subtraction
    private static final int MUL_MIN  = 2,  MUL_MAX  = 999;   // each factor range for multiplication
    private static final int DIV_MIN  = 2,  DIV_MAX  = 999;   // divisor & quotient range for division
    // ──────────────────────────────────────────────────────────────────────────

    private final DailyChallengeRepository challengeRepository;
    private final DailyChallengeResultRepository resultRepository;
    private final DailyChallengeQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public DailyChallengeService(DailyChallengeRepository challengeRepository,
                                  DailyChallengeResultRepository resultRepository,
                                  DailyChallengeQuestionRepository questionRepository,
                                  UserRepository userRepository,
                                  StreakService streakService) {
        this.challengeRepository = challengeRepository;
        this.resultRepository = resultRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.streakService = streakService;
    }

    public DailyChallengeDto getTodayChallenge(String uid) {
        LocalDate today = LocalDate.now();
        DailyChallenge challenge = challengeRepository.findByChallengeDate(today)
                .orElseGet(() -> generateChallenge(today));

        List<DailyChallengeQuestion> questions = questionRepository.findByChallengeDate(today);
        if (questions.isEmpty()) {
            questions = generateQuestions(today);
        }

        DailyChallengeDto dto = new DailyChallengeDto();
        dto.setDate(today);
        dto.setOperation("mixed");
        dto.setQuestionCount(challenge.getQuestionCount());
        dto.setRangeMin(challenge.getRangeMin());
        dto.setRangeMax(challenge.getRangeMax());

        Optional<DailyChallengeResult> result = resultRepository.findByFirebaseUidAndChallengeDate(uid, today);
        dto.setCompleted(result.isPresent());
        result.ifPresent(r -> dto.setUserScore(r.getScore()));

        List<ChallengeQuestionDto> questionDtos = questions.stream()
                .sorted(Comparator.comparingInt(DailyChallengeQuestion::getQuestionNumber))
                .map(q -> new ChallengeQuestionDto(
                        q.getQuestionNumber(),
                        buildQuestionText(q),
                        String.valueOf(q.getAnswer())
                ))
                .collect(Collectors.toList());
        dto.setQuestions(questionDtos);

        return dto;
    }

    @Transactional
    public DailyChallengeResultDto submitChallenge(String uid, DailyChallengeSubmitDto dto) {
        LocalDate today = LocalDate.now();

        if (resultRepository.findByFirebaseUidAndChallengeDate(uid, today).isPresent()) {
            throw new RuntimeException("Already submitted today challenge");
        }

        DailyChallengeResult result = new DailyChallengeResult();
        result.setFirebaseUid(uid);
        result.setChallengeDate(today);
        result.setScore(dto.getScore());
        result.setCorrectAnswers(dto.getCorrectAnswers());
        resultRepository.save(result);

        int newStreak = 0, longestStreak = 0;
        Optional<com.mathlit.backend.model.User> userOpt = userRepository.findByFirebaseUid(uid);
        if (userOpt.isPresent()) {
            com.mathlit.backend.model.User user = userOpt.get();
            StreakStatusDto streakStatus = streakService.updateStreak(user);
            userRepository.save(user);
            newStreak = streakStatus.getCurrentStreak();
            longestStreak = streakStatus.getLongestStreak();
        }

        long totalParticipants = resultRepository.countByChallengeDate(today);
        long betterThanMe = resultRepository.countByChallengeDateAndScoreGreaterThan(today, dto.getScore());
        long rank = betterThanMe + 1;
        double percentile = totalParticipants > 1
                ? (double)(totalParticipants - betterThanMe) / totalParticipants * 100
                : 100.0;

        return new DailyChallengeResultDto(rank, totalParticipants, dto.getScore(), percentile, newStreak, longestStreak);
    }

    public boolean hasCompletedToday(String uid) {
        return resultRepository.findByFirebaseUidAndChallengeDate(uid, LocalDate.now()).isPresent();
    }

    public boolean questionsExistForDate(LocalDate date) {
        return questionRepository.existsByChallengeDate(date);
    }

    @Transactional
    public DailyChallenge generateChallenge(LocalDate date) {
        DailyChallenge challenge = new DailyChallenge();
        challenge.setChallengeDate(date);
        challenge.setOperation("mixed");
        challenge.setQuestionCount(20);
        challenge.setRangeMin(1);
        challenge.setRangeMax(50);
        DailyChallenge saved = challengeRepository.save(challenge);
        generateQuestions(date);
        return saved;
    }

    @Transactional
    public List<DailyChallengeQuestion> generateQuestions(LocalDate date) {
        Random rng = new Random(date.toEpochDay());
        List<DailyChallengeQuestion> questions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            int a = rng.nextInt(ADD_MAX - ADD_MIN + 1) + ADD_MIN;
            int b = rng.nextInt(ADD_MAX - ADD_MIN + 1) + ADD_MIN;
            questions.add(buildQ(date, a, b, "ADD", a + b));
        }
        for (int i = 0; i < 5; i++) {
            int b = rng.nextInt(SUB_MAX - SUB_MIN + 1) + SUB_MIN;
            int a = b + rng.nextInt(SUB_MAX - SUB_MIN + 1) + SUB_MIN;
            questions.add(buildQ(date, a, b, "SUB", a - b));
        }
        for (int i = 0; i < 5; i++) {
            int a = rng.nextInt(MUL_MAX - MUL_MIN + 1) + MUL_MIN;
            int b = rng.nextInt(MUL_MAX - MUL_MIN + 1) + MUL_MIN;
            questions.add(buildQ(date, a, b, "MUL", a * b));
        }
        for (int i = 0; i < 5; i++) {
            int divisor = rng.nextInt(DIV_MAX - DIV_MIN + 1) + DIV_MIN;
            int quotient = rng.nextInt(DIV_MAX - DIV_MIN + 1) + DIV_MIN;
            questions.add(buildQ(date, divisor * quotient, divisor, "DIV", quotient));
        }

        Collections.shuffle(questions, new Random(date.toEpochDay() + 1));
        for (int i = 0; i < questions.size(); i++) questions.get(i).setQuestionNumber(i + 1);

        return questionRepository.saveAll(questions);
    }

    private DailyChallengeQuestion buildQ(LocalDate date, int op1, int op2, String op, int ans) {
        DailyChallengeQuestion q = new DailyChallengeQuestion();
        q.setChallengeDate(date);
        q.setOperand1(op1);
        q.setOperand2(op2);
        q.setOperation(op);
        q.setAnswer(ans);
        return q;
    }

    private String buildQuestionText(DailyChallengeQuestion q) {
        String sym;
        switch (q.getOperation()) {
            case "ADD": sym = "+"; break;
            case "SUB": sym = "-"; break;
            case "MUL": sym = "x"; break;
            case "DIV": sym = "/"; break;
            default:     sym = "?";
        }
        return q.getOperand1() + " " + sym + " " + q.getOperand2();
    }
}



