package com.mathlit.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_challenge_results",
    uniqueConstraints = @UniqueConstraint(columnNames = {"firebase_uid", "challenge_date"}))
@Data
public class DailyChallengeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, length = 128)
    private String firebaseUid;

    @Column(name = "challenge_date", nullable = false)
    private LocalDate challengeDate;

    private int score = 0;

    @Column(name = "correct_answers")
    private int correctAnswers = 0;

    @CreationTimestamp
    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;
}
