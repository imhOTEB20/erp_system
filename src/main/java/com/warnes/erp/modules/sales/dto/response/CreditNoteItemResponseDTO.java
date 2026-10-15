package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Detalle de un producto devuelto en la Nota de Crédito")
public record CreditNoteItemResponseDTO(
        @Schema(description = "UUID del producto", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID productUuid,
        @Schema(description = "SKU Interno", example = "BOSCH-BAT-75A")
        String productSku,
        @Schema(description = "Nombre del producto", example = "Batería Bosch S4 12V 75Ah")
        String productName,
        @Schema(description = "Cantidad devuelta", example = "1.00")
        BigDecimal quantity,
        @Schema(description = "Precio unitario original", example = "75000.00")
        BigDecimal unitPrice,
        @Schema(description = "Subtotal devuelto", example = "75000.00")
        BigDecimal subtotal
) {}