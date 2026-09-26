package com.warnes.erp.modules.catalog.mapper;

import com.warnes.erp.modules.catalog.dto.request.ProductCrossReferenceRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductUpdateRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductCrossReferenceResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.entity.ProductCrossReference;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // 1. Inyectamos la ejecución del método default para calcular el margen en el Response
    @Mapping(target = "profitMargin", expression = "java(calculateMargin(entity.getCostPrice(), entity.getSalePrice()))")
    ProductResponseDTO toResponseDTO(Product entity);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "taxType", ignore = true)
    @Mapping(target = "crossReferences", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "taxType", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "crossReferences", ignore = true)
    void updateEntityFromDto(ProductUpdateRequestDTO dto, @MappingTarget Product entity);

    ProductCrossReferenceResponseDTO toCrossReferenceResponseDTO(ProductCrossReference entity);

    @Mapping(target = "product", ignore = true)
    ProductCrossReference toCrossReferenceEntity(ProductCrossReferenceRequestDTO dto);

    // 2. Método default de MapStruct para el cálculo en memoria
    default BigDecimal calculateMargin(BigDecimal costPrice, BigDecimal salePrice) {
        if (costPrice == null || salePrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // Formula: ((SalePrice - CostPrice) / CostPrice) * 100
        return salePrice.subtract(costPrice)
                .divide(costPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}