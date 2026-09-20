package com.warnes.erp.modules.catalog.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponseDTO(
        UUID uuid,
        String sku,
        String barcode,
        String name,
        CategoryResponseDTO category,
        BrandResponseDTO brand,
        boolean service,
        boolean kit,
        BigDecimal costPrice,
        BigDecimal profitMargin,
        BigDecimal salePrice,
        BigDecimal taxPercentage,
        List<ProductCrossReferenceResponseDTO> crossReferences
) {}