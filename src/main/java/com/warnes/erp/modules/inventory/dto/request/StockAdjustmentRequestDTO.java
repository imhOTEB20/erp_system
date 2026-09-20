package com.warnes.erp.modules.inventory.dto.request;

import com.warnes.erp.modules.inventory.entity.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @NotNull(message = "El almacén es obligatorio")
        UUID warehouseUuid,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        MovementType movementType,

        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity
) {}