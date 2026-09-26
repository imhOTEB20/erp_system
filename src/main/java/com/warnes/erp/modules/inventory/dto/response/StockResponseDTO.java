package com.warnes.erp.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Stock y cantidad minima de un producto para una sucursal en especifico.")
public record StockResponseDTO(
        @Schema(description = "Cantidad diponible", example = "10.00")
        BigDecimal quantity,

        @Schema(description = "Minimo aceptable de un producto en el almacen")
        BigDecimal minStock
) {
}
