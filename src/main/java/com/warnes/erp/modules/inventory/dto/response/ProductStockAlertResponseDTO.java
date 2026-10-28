package com.warnes.erp.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Datos de un producto con stock por debajo del minimo.")
public record ProductStockAlertResponseDTO(
        @Schema(description = "UUID del producto")
        UUID productUuid,

        @Schema(description = "Nombre del producto")
        String productName,

        @Schema(description = "UUID de la sucursal")
        UUID warehouseUuid,

        @Schema(description = "Nombre de la sucursal")
        String warehouseName,

        @Schema(description = "Datos del stock")
        StockResponseDTO stockResponseDTO
) {

}
