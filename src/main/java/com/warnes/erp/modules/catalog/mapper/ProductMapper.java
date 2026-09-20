package com.warnes.erp.modules.catalog.mapper;

import com.warnes.erp.modules.catalog.dto.request.ProductCrossReferenceRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductCrossReferenceResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.entity.ProductCrossReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, BrandMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductResponseDTO toResponseDTO(Product entity);

    // Ignoramos Category y Brand porque los buscaremos en la BD dentro del Service
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "crossReferences", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    ProductCrossReferenceResponseDTO toCrossReferenceResponseDTO(ProductCrossReference entity);

    @Mapping(target = "product", ignore = true)
    ProductCrossReference toCrossReferenceEntity(ProductCrossReferenceRequestDTO dto);
}