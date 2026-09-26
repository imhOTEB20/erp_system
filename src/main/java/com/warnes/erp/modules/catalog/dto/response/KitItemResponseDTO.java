package com.warnes.erp.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Detalle de un producto individual que compone un Kit/Combo")
public record KitItemResponseDTO(
        @Schema(description = "UUID del producto componente", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID childProductUuid,

        @Schema(description = "SKU interno del componente", example = "BUJ-NGK-BPR6ES")
        String childProductSku,

        @Schema(description = "Nombre descriptivo del componente", example = "Bujía NGK BPR6ES")
        String childProductName,

        @Schema(description = "Cantidad de este componente dentro del kit", example = "4.00")
        BigDecimal quantity,

        @Schema(description = "Costo base de una unidad de este componente", example = "2500.00")
        BigDecimal unitCost,

        @Schema(description = "Costo subtotal (Costo Unitario * Cantidad)", example = "10000.00")
        BigDecimal subtotalCost
) {}