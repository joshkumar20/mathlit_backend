package com.mathlit.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_section_category", columnList = "section,category"),
        @Index(name = "idx_questions_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_no")
    private Integer questionNo;

    @Column(nullable = false)
    private String section;   // ARITHMETIC / ALGEBRA / GEOMETRY / TRIGONOMETRY / STATISTICS

    @Column(nullable = false)
    private String category;  // SIMPLIFICATION / RATIO / AREA / MEAN etc.

    @Column(nullable = false)
    private String difficulty; // EASY / MEDIUM / HARD

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "option_a", nullable = false)
    private String optionA;

    @Column(name = "option_b", nullable = false)
    private String optionB;

    @Column(name = "option_c", nullable = false)
    private String optionC;

    @Column(name = "option_d", nullable = false)
    private String optionD;

    @Column(name = "correct_index", nullable = false)
    private Integer correctIndex; // 0=A, 1=B, 2=C, 3=D

    @Column(columnDefinition = "TEXT", nullable = false)
    private String solution;

    @Column(name = "question_image_url")
    private String questionImageUrl; // nullable — remote URL, shown in UI only when present

    @Column(name = "solution_image_url")
    private String solutionImageUrl; // nullable — remote URL, shown in UI only when present


    @Column(name = "exam_source", length = 150)
    private String examSource; // e.g. "SSC CGL 2023 Shift 2", nullable
    
    @Column(name = "tag", length = 150)
    private String tag; // e.g. "SSC CGL 2023 Shift 2", nullable

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}