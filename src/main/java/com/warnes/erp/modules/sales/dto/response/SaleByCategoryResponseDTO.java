package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Cantidad de ventas y total de una categoria")
public record SaleByCategoryResponseDTO(
        @Schema(description = "Nombre de la categoria")
        String categoryName,

        @Schema(description = "Total vendido")
        BigDecimal totalAmount,

        @Schema(description = "Total de ventas realizadas")
        BigDecimal quantitySold
) {
}
