package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.CategoryRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.CategoryResponseDTO;
import com.warnes.erp.modules.catalog.entity.Category;
import com.warnes.erp.modules.catalog.mapper.CategoryMapper;
import com.warnes.erp.modules.catalog.repository.CategoryRepository;
import com.warnes.erp.modules.catalog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toResponseDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(UUID uuid, CategoryRequestDTO request) {
        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        category.setName(request.name());
        category.setActive(request.active());

        return categoryMapper.toResponseDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryByUuid(UUID uuid) {
        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        return categoryMapper.toResponseDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(boolean includeInactive) {
        List<Category> categories = includeInactive ?
                categoryRepository.findAll() :
                categoryRepository.findAll().stream().filter(Category::isActive).toList();

        return categories.stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCategory(UUID uuid) {
        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Soft Delete estructurado
        category.setDeletedAt(ZonedDateTime.now());
        category.setActive(false);
        categoryRepository.save(category);
    }
}