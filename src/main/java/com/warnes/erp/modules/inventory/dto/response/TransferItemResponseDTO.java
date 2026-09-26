package com.warnes.erp.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Detalle del producto incluido en una orden de transferencia")
public record TransferItemResponseDTO(
        @Schema(description = "UUID del producto a transferir", example = "770e8400-e29b-41d4-a716-446655440000")
        UUID productUuid,

        @Schema(description = "SKU Interno", example = "BOSCH-BAT-75A")
        String productSku,

        @Schema(description = "Nombre descriptivo", example = "Batería Bosch S4 12V 75Ah")
        String productName,

        @Schema(description = "Cantidad enviada", example = "10")
        BigDecimal quantity
) {}