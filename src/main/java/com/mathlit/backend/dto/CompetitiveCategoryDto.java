package com.mathlit.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Tree-node DTO for the Competitive section structure.
 *
 * Root  → children are mid-level tabs (PYQ, Mocks).
 * Mid   → children are leaf items.
 * Leaf  → children is null/empty; carries question-fetch params.
 */
@Getter @Setter @NoArgsConstructor
public class CompetitiveCategoryDto {

    private Long id;
    private String name;
    private String slug;
    private String icon;
    private int displayOrder;

    // Only present on leaf nodes
    private String qSection;
    private String qCategory;
    private String qTag;
    private String qExamSource;
    private Integer questionCount;

    // Populated for root and mid nodes; null/empty for leaf nodes
    private List<CompetitiveCategoryDto> children;
}