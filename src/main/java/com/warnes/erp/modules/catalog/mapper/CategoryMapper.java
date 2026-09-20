package com.warnes.erp.modules.catalog.mapper;

import com.warnes.erp.modules.catalog.dto.request.CategoryRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.CategoryResponseDTO;
import com.warnes.erp.modules.catalog.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    CategoryResponseDTO toResponseDTO(Category entity);
    Category toEntity(CategoryRequestDTO dto);
}