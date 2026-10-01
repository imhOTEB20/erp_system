package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.ElementNotDeletedException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.CategoryRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.CategoryResponseDTO;
import com.warnes.erp.modules.catalog.entity.Brand;
import com.warnes.erp.modules.catalog.entity.Category;
import com.warnes.erp.modules.catalog.mapper.CategoryMapper;
import com.warnes.erp.modules.catalog.repository.BrandSpecification;
import com.warnes.erp.modules.catalog.repository.CategoryRepository;
import com.warnes.erp.modules.catalog.repository.CategorySpecification;
import com.warnes.erp.modules.catalog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
        Category category = findByUuid(uuid);

        category.setName(request.name());
        category.setActive(request.active());

        return categoryMapper.toResponseDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryByUuid(UUID uuid) {
        Category category = findByUuid(uuid);
        return categoryMapper.toResponseDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(Boolean isActive, Boolean includeDeleted) {
        Specification<Category> filters = CategorySpecification.withDynamicFilters(isActive, includeDeleted);
        List<Category> categories = categoryRepository.findAll(filters);
        return categories.stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCategory(UUID uuid) {
        Category category = findByUuid(uuid);

        // Soft Delete estructurado
        category.setDeletedAt(ZonedDateTime.now());
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO restoreCategory(UUID uuid) {
        Category category = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        if (category.getDeletedAt() != null) {
            category.setDeletedAt(null);
            category.setActive(true);
        } else {
            throw new ElementNotDeletedException("El brand no esta eliminado.");
        }

        return categoryMapper.toResponseDTO(
                categoryRepository.save(category)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllDeleted() {
        Specification<Category> filter = CategorySpecification.deleted();
        return categoryRepository.findAll(filter).stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean toggleAvailability(UUID uuid) {
        Category category = findByUuid(uuid);
        category.setActive(!category.isActive());

        categoryRepository.save(category);
        return category.isActive();
    }

    private Category findByUuid(UUID uuid) {
        Specification<Category> filter = Specification
                .where(CategorySpecification.hasUuid(uuid))
                .and(CategorySpecification.notDeleted());

        return categoryRepository.findOne(filter)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la categoria."));
    }
}