package com.mathlit.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Self-referencing tree that describes the configurable Competitive section:
 *
 *   Root nodes  (parentId = null)  → top-level slider tabs  e.g. SSC, BANK, RRB
 *   Mid nodes   (depth = 1)        → sub-tabs               e.g. PYQ, Mocks
 *   Leaf nodes  (depth = 2, isLeaf = true) → tap-to-play    e.g. SSC CHSL Shift 1
 *
 * Leaf nodes carry the question-fetch parameters so the app knows which
 * questions to load from the questions table.
 */
@Entity
@Table(name = "competitive_categories",
        indexes = { @Index(name = "idx_comp_cat_parent", columnList = "parent_id") })
@Getter @Setter @NoArgsConstructor
public class CompetitiveCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name shown in the UI (e.g. "SSC", "PYQ", "SSC CHSL Shift 1") */
    @Column(nullable = false, length = 100)
    private String name;

    /** URL-safe identifier used for question lookup (e.g. "SSC", "PYQ", "SSC_CHSL_SHIFT_1") */
    @Column(nullable = false, length = 100, unique = false)
    private String slug;

    /** Emoji or short symbol shown as icon in the UI (nullable) */
    @Column(length = 20)
    private String icon;

    /** null → this is a root node. Non-null → child node */
    @Column(name = "parent_id")
    private Long parentId;

    /** Controls ordering within siblings */
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    /** When false, hidden from the app (soft-delete / disable without deleting) */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ── Question-fetch params — only set on LEAF nodes ────────────────────────

    /**
     * section value in the questions table to filter by (e.g. "COMPETITIVE").
     * Null on non-leaf nodes.
     */
    @Column(name = "q_section", length = 100)
    private String qSection;

    /**
     * category value in the questions table (e.g. "SSC").
     * Null on non-leaf nodes.
     */
    @Column(name = "q_category", length = 100)
    private String qCategory;

    /**
     * tag value in the questions table (e.g. "PYQ").
     * Null on non-leaf nodes.
     */
    @Column(name = "q_tag", length = 100)
    private String qTag;

    /**
     * examSource value in the questions table (e.g. "SSC_CHSL_SHIFT_1").
     * Null on non-leaf nodes.
     */
    @Column(name = "q_exam_source", length = 150)
    private String qExamSource;

    /** Total number of questions available (pre-computed or lazy — stored for display) */
    @Column(name = "question_count")
    private Integer questionCount;
}