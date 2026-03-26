package com.mathlit.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_challenges")
@Data
public class DailyChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenge_date", unique = true, nullable = false)
    private LocalDate challengeDate;

    @Column(length = 50)
    private String operation;

    @Column(name = "question_count")
    private int questionCount = 20;

    @Column(name = "range_min")
    private int rangeMin = 1;

    @Column(name = "range_max")
    private int rangeMax = 50;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
