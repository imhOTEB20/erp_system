package com.warnes.erp.modules.purchases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Detalle de un producto específico dentro de una factura de compra")
public record PurchaseItemResponseDTO(
        @Schema(description = "UUID del producto", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID productUuid,

        @Schema(description = "SKU interno del producto", example = "AMORT-FR-001")
        String productSku,

        @Schema(description = "Nombre descriptivo del producto", example = "Amortiguador Fric-Rot Delantero")
        String productName,

        @Schema(description = "Cantidad que ingresó", example = "50.00")
        BigDecimal quantity,

        @Schema(description = "Costo unitario registrado", example = "14500.00")
        BigDecimal unitCost
) {}