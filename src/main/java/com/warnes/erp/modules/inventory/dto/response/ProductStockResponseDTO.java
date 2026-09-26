package com.warnes.erp.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Stock de un producto")
public record ProductStockResponseDTO(
        @Schema(description = "UUID del producto")
        UUID productUuid,

        @Schema(description = "Nombre del producto")
        String productName,

        @Schema(description = "Datos del stock")
        StockResponseDTO stockResponseDTO
) {

}
