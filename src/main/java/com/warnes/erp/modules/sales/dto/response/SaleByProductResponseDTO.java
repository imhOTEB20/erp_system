package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ventas de un producto en particular.")
public record SaleByProductResponseDTO(
        @Schema(description = "SKU del producto.", example = "BOSCH-BAT-75A")
        String productSku,
        @Schema(description = "Nombre del producto.", example = "Batería Bosch S4 12V 75Ah")
        String productName,
        @Schema(description = "Cantidades vendidas.")
        BigDecimal quantitySold,
        @Schema(description = "Total vendido.")
        BigDecimal totalAmount
) {
}
