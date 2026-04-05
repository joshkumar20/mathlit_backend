package com.mathlit.backend.repository;

import com.mathlit.backend.model.CompetitiveCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetitiveCategoryRepository extends JpaRepository<CompetitiveCategory, Long> {

    /** All active root nodes (parentId is null), ordered by displayOrder */
    List<CompetitiveCategory> findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    /** Active children of a given parent, ordered by displayOrder */
    List<CompetitiveCategory> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);

    /** All children (active or inactive) of a given parent */
    List<CompetitiveCategory> findByParentId(Long parentId);
}