package com.warnes.erp.modules.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload del detalle de un repuesto dentro de una orden de venta")
public record SaleItemRequestDTO(
        @Schema(description = "UUID del producto a vender", example = "770e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @Schema(description = "Cantidad vendida", example = "2.00")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity
) {}