package com.mathlit.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_question",
                columnNames = {"firebase_uid", "question_id"}
        ),
        indexes = {
                @Index(name = "idx_uqp_uid", columnList = "firebase_uid"),
                @Index(name = "idx_uqp_uid_attempted", columnList = "firebase_uid,is_attempted")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserQuestionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false)
    private String firebaseUid;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    // false = not attempted yet (appears in next game)
    // true  = already attempted (skipped in next game unless marked for reattempt)
    @Column(name = "is_attempted", nullable = false)
    private boolean isAttempted = false;

    // When user marks for reattempt → is_attempted set back to false
    // so question reappears in next game
    @Column(name = "is_marked_reattempt", nullable = false)
    private boolean isMarkedReattempt = false;

    @Column(name = "last_attempted_at")
    private LocalDateTime lastAttemptedAt;
}