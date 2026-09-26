package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Detalle facturado de un repuesto dentro de un comprobante de venta")
public record SaleItemResponseDTO(
        @Schema(description = "UUID del repuesto", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID productUuid,

        @Schema(description = "Código SKU", example = "BOSCH-BAT-75A")
        String productSku,

        @Schema(description = "Descripción", example = "Batería Bosch S4 12V 75Ah")
        String productName,

        @Schema(description = "Cantidad vendida (BigDecimal para soportar fracciones)", example = "2.00")
        BigDecimal quantity, // Refactorizado de int a BigDecimal

        @Schema(description = "Precio unitario final aplicado en mostrador", example = "75000.00")
        BigDecimal unitPrice,

        @Schema(description = "Subtotal (Cantidad * Precio Unitario)", example = "150000.00")
        BigDecimal subtotal
) {}