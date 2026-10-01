package com.warnes.erp.modules.catalog.service;

import com.warnes.erp.modules.catalog.dto.request.CategoryRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.CategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO request);
    CategoryResponseDTO updateCategory(UUID uuid, CategoryRequestDTO request);
    CategoryResponseDTO getCategoryByUuid(UUID uuid);
    List<CategoryResponseDTO> getAllCategories(Boolean isActive, Boolean includeDeleted);
    void deleteCategory(UUID uuid);
    Boolean toggleAvailability(UUID uuid);
    CategoryResponseDTO restoreCategory(UUID uuid);
    List<CategoryResponseDTO> getAllDeleted();
}