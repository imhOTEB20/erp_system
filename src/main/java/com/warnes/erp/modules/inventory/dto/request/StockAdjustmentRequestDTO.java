package com.warnes.erp.modules.inventory.dto.request;

import com.warnes.erp.modules.inventory.entity.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload para registrar un ajuste manual de inventario (positivo o negativo)")
public record StockAdjustmentRequestDTO(
        @Schema(description = "UUID del producto a ajustar", example = "770e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @Schema(description = "UUID del almacén físico donde se detectó el descuadre", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "El almacén es obligatorio")
        UUID warehouseUuid,

        @Schema(description = "Tipo de movimiento de ajuste a realizar", example = "ADJUSTMENT")
        @NotNull(message = "El tipo de movimiento es obligatorio")
        MovementType movementType,

        @Schema(description = "Cantidad a ajustar", example = "5.00")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity
) {}