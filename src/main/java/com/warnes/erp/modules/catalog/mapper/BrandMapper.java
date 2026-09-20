package com.warnes.erp.modules.catalog.mapper;

import com.warnes.erp.modules.catalog.dto.request.BrandRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
import com.warnes.erp.modules.catalog.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    BrandResponseDTO toResponseDTO(Brand entity);
    Brand toEntity(BrandRequestDTO dto);
}