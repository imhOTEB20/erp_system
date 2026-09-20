package com.warnes.erp.modules.catalog.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRequestDTO(
        @NotBlank(message = "El SKU es obligatorio")
        @Size(max = 50)
        String sku,

        @Size(max = 64)
        String barcode,

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 200)
        String name,

        @NotNull(message = "El UUID de la categoría es obligatorio")
        UUID categoryUuid,

        @NotNull(message = "El UUID de la marca es obligatorio")
        UUID brandUuid,

        @NotNull(message = "Indicar si es servicio es obligatorio")
        Boolean service,

        @NotNull(message = "Indicar si es kit es obligatorio")
        Boolean kit,

        @NotNull(message = "El precio de costo es obligatorio")
        @DecimalMin(value = "0.0000")
        BigDecimal costPrice,

        @NotNull(message = "El margen de ganancia es obligatorio")
        @DecimalMin(value = "0.00")
        BigDecimal profitMargin,

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.00")
        BigDecimal salePrice,

        @NotNull(message = "El porcentaje de impuesto es obligatorio")
        @DecimalMin(value = "0.00")
        BigDecimal taxPercentage,

        @Valid
        List<ProductCrossReferenceRequestDTO> crossReferences
) {}