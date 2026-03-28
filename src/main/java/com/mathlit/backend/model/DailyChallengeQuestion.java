package com.mathlit.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "daily_challenge_questions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"challenge_date", "question_number"}))
@Data
public class DailyChallengeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenge_date", nullable = false)
    private LocalDate challengeDate;

    @Column(name = "question_number", nullable = false)
    private int questionNumber;

    @Column(nullable = false)
    private int operand1;

    @Column(nullable = false)
    private int operand2;

    @Column(nullable = false, length = 20)
    private String operation; // ADD, SUB, MUL, DIV

    @Column(nullable = false)
    private int answer;
}