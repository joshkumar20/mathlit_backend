package com.mathlit.backend.repository;

import com.mathlit.backend.model.DailyChallengeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyChallengeQuestionRepository extends JpaRepository<DailyChallengeQuestion, Long> {
    List<DailyChallengeQuestion> findByChallengeDate(LocalDate date);
    boolean existsByChallengeDate(LocalDate date);
}