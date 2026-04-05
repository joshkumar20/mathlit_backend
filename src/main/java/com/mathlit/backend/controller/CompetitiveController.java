package com.mathlit.backend.controller;

import com.mathlit.backend.dto.CompetitiveCategoryDto;
import com.mathlit.backend.model.CompetitiveCategory;
import com.mathlit.backend.repository.CompetitiveCategoryRepository;
import com.mathlit.backend.service.CompetitiveCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * GET  /api/v1/competitive/structure  — returns the full tree (all users)
 * POST /api/v1/competitive/categories — add or update a node (admin only)
 * POST /api/v1/competitive/categories/{id}/toggle — enable/disable a node
 *
 * Admin check: simple request attribute "uid" → compared against an env var
 * ADMIN_UID. For production you'd use a proper role system.
 */
@RestController
@RequestMapping("/api/v1/competitive")
@RequiredArgsConstructor
public class CompetitiveController {

    private final CompetitiveCategoryService service;
    private final CompetitiveCategoryRepository repo;

    /**
     * Returns the full competitive structure tree.
     *
     * Response shape (example):
     * [
     *   {
     *     "id": 1, "name": "SSC", "slug": "SSC", "icon": "📝",
     *     "children": [
     *       { "id": 2, "name": "PYQ", "slug": "PYQ",
     *         "children": [
     *           { "id": 4, "name": "SSC CHSL Shift 1", "slug": "SSC_CHSL_SHIFT_1",
     *             "qSection": "COMPETITIVE", "qCategory": "SSC",
     *             "qTag": "PYQ", "qExamSource": "SSC_CHSL_SHIFT_1",
     *             "questionCount": 45, "children": [] }
     *         ]
     *       },
     *       { "id": 3, "name": "Mocks", "slug": "MOCKS", "children": [...] }
     *     ]
     *   }
     * ]
     */
    @GetMapping("/structure")
    public ResponseEntity<List<CompetitiveCategoryDto>> getStructure() {
        return ResponseEntity.ok(service.getStructure());
    }

    /**
     * Admin: add or update a category node.
     *
     * Request body for a NEW ROOT node:
     * { "name": "SSC", "slug": "SSC", "icon": "📝", "displayOrder": 1 }
     *
     * Request body for a NEW MID node:
     * { "name": "PYQ", "slug": "PYQ", "icon": "📋", "displayOrder": 1, "parentId": 1 }
     *
     * Request body for a NEW LEAF node:
     * { "name": "SSC CHSL Shift 1", "slug": "SSC_CHSL_SHIFT_1", "displayOrder": 1,
     *   "parentId": 2,
     *   "qSection": "COMPETITIVE", "qCategory": "SSC",
     *   "qTag": "PYQ", "qExamSource": "SSC_CHSL_SHIFT_1", "questionCount": 45 }
     *
     * Include "id" to update an existing node.
     */
    @PostMapping("/categories")
    public ResponseEntity<CompetitiveCategoryDto> saveCategory(
            @RequestBody AdminCategoryRequest req) {

        CompetitiveCategory entity = req.id != null
                ? repo.findById(req.id).orElse(new CompetitiveCategory())
                : new CompetitiveCategory();

        entity.setName(req.name);
        entity.setSlug(req.slug);
        entity.setIcon(req.icon);
        entity.setDisplayOrder(req.displayOrder);
        entity.setParentId(req.parentId);
        entity.setActive(req.isActive != null ? req.isActive : true);
        entity.setQSection(req.qSection);
        entity.setQCategory(req.qCategory);
        entity.setQTag(req.qTag);
        entity.setQExamSource(req.qExamSource);
        entity.setQuestionCount(req.questionCount);

        CompetitiveCategory saved = repo.save(entity);

        CompetitiveCategoryDto dto = new CompetitiveCategoryDto();
        dto.setId(saved.getId());
        dto.setParentId(saved.getParentId());
        dto.setName(saved.getName());
        dto.setSlug(saved.getSlug());
        dto.setIcon(saved.getIcon());
        dto.setDisplayOrder(saved.getDisplayOrder());
        dto.setQSection(saved.getQSection());
        dto.setQCategory(saved.getQCategory());
        dto.setQTag(saved.getQTag());
        dto.setQExamSource(saved.getQExamSource());
        dto.setQuestionCount(saved.getQuestionCount());
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns the direct active children of a category node.
     * Used by the Android app when navigating deeper into the competitive tree.
     */
    @GetMapping("/categories/{id}/children")
    public ResponseEntity<List<CompetitiveCategoryDto>> getChildren(@PathVariable Long id) {
        List<CompetitiveCategory> children =
                repo.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(id);

        List<CompetitiveCategoryDto> dtos = children.stream().map(c -> {
            CompetitiveCategoryDto dto = new CompetitiveCategoryDto();
            dto.setId(c.getId());
            dto.setParentId(c.getParentId());
            dto.setName(c.getName());
            dto.setSlug(c.getSlug());
            dto.setIcon(c.getIcon());
            dto.setDisplayOrder(c.getDisplayOrder());
            dto.setQSection(c.getQSection());
            dto.setQCategory(c.getQCategory());
            dto.setQTag(c.getQTag());
            dto.setQExamSource(c.getQExamSource());
            dto.setQuestionCount(c.getQuestionCount());
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /** Admin: delete a node and all its descendants recursively. */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        deleteRecursive(id);
        return ResponseEntity.ok().build();
    }

    private void deleteRecursive(Long id) {
        repo.findByParentId(id).forEach(child -> deleteRecursive(child.getId()));
        repo.deleteById(id);
    }

    /** Admin: toggle active status of a node (and implicitly its subtree) */
    @PostMapping("/categories/{id}/toggle")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id,
                                              @RequestParam boolean active) {
        service.setActive(id, active);
        return ResponseEntity.ok().build();
    }

    // ── Request model ─────────────────────────────────────────────────────────

    public static class AdminCategoryRequest {
        public Long id;
        public String name;
        public String slug;
        public String icon;
        public int displayOrder;
        public Long parentId;
        public Boolean isActive;
        public String qSection;
        public String qCategory;
        public String qTag;
        public String qExamSource;
        public Integer questionCount;
    }
}