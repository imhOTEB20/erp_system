package com.warnes.erp.modules.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload del detalle de un producto a transferir")
public record TransferItemRequestDTO(
        @Schema(description = "UUID del producto a mover", example = "770e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @Schema(description = "Cantidad física a descontar del origen y sumar al destino", example = "10.00")
        @DecimalMin(value = "1.0", message = "La cantidad a transferir debe ser mayor a 0")
        BigDecimal quantity
) {}