package com.warnes.erp.modules.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request para inicializar el stock de un producto en una sucursal.")
public record InitialStockRequestDTO(

        @NotNull
        @Schema(description = "UUID del producto")
        UUID productUuid,

        @NotNull
        @Schema(description = "UUID de la sucursal")
        UUID warehouseUuid,

        @Schema(description = "stock inicial")
        BigDecimal quantity,

        @Schema(description = "stock minimo")
        BigDecimal minStock
) {
    public InitialStockRequestDTO {
        if (quantity == null) {
            quantity = new BigDecimal(1);
        }
        if (minStock == null) {
            minStock = BigDecimal.ZERO;
        }
    }
}
