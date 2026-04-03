package com.mathlit.backend.service;

import com.mathlit.backend.dto.CompetitiveCategoryDto;
import com.mathlit.backend.model.CompetitiveCategory;
import com.mathlit.backend.repository.CompetitiveCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetitiveCategoryService {

    private final CompetitiveCategoryRepository repo;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Returns the full tree: root nodes with their mid-children,
     * each mid-child with its leaf-children.
     */
    @Transactional(readOnly = true)
    public List<CompetitiveCategoryDto> getStructure() {
        List<CompetitiveCategory> roots =
                repo.findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

        return roots.stream()
                .map(root -> toDto(root, 0))
                .collect(Collectors.toList());
    }

    /**
     * Upsert a category. If dto.id is non-null, updates; otherwise creates.
     */
    @Transactional
    public CompetitiveCategoryDto save(CompetitiveCategoryDto dto) {
        CompetitiveCategory entity = dto.getId() != null
                ? repo.findById(dto.getId()).orElse(new CompetitiveCategory())
                : new CompetitiveCategory();

        entity.setName(dto.getName());
        entity.setSlug(dto.getSlug());
        entity.setIcon(dto.getIcon());
        entity.setDisplayOrder(dto.getDisplayOrder());
        entity.setActive(true);

        // parentId: inferred from the request payload (caller sets it)
        // We accept it via a helper field on the DTO — re-use qSection as sentinel?
        // Simpler: admin endpoint receives parentId separately.
        // For now, qSection/qCategory/qTag/qExamSource set on leaf:
        entity.setQSection(dto.getQSection());
        entity.setQCategory(dto.getQCategory());
        entity.setQTag(dto.getQTag());
        entity.setQExamSource(dto.getQExamSource());
        entity.setQuestionCount(dto.getQuestionCount());

        return toDto(repo.save(entity), 0);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        repo.findById(id).ifPresent(c -> {
            c.setActive(active);
            repo.save(c);
        });
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────

    private CompetitiveCategoryDto toDto(CompetitiveCategory c, int depth) {
        CompetitiveCategoryDto dto = new CompetitiveCategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setSlug(c.getSlug());
        dto.setIcon(c.getIcon());
        dto.setDisplayOrder(c.getDisplayOrder());
        dto.setQSection(c.getQSection());
        dto.setQCategory(c.getQCategory());
        dto.setQTag(c.getQTag());
        dto.setQExamSource(c.getQExamSource());
        dto.setQuestionCount(c.getQuestionCount());

        // Recurse max 2 levels deep (root → mid → leaf)
        if (depth < 2) {
            List<CompetitiveCategory> children =
                    repo.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(c.getId());
            dto.setChildren(children.stream()
                    .map(child -> toDto(child, depth + 1))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}