package com.mathlit.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
@Data
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, length = 128)
    private String firebaseUid;

    @Column(length = 50)
    private String operation;

    @Column(name = "game_mode", length = 50)
    private String gameMode;

    private int score = 0;

    @Column(name = "correct_answers")
    private int correctAnswers = 0;

    @Column(name = "wrong_answers")
    private int wrongAnswers = 0;

    @Column(name = "total_questions")
    private int totalQuestions = 0;

    private double accuracy = 0.0;

    @Column(name = "duration_secs")
    private int durationSecs = 0;

    @CreationTimestamp
    @Column(name = "played_at", updatable = false)
    private LocalDateTime playedAt;
}
