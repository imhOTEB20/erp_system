package com.warnes.erp.modules.cash.dto.request;

import com.warnes.erp.modules.cash.entity.CashMovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CashMovementRequestDTO(
        @NotNull(message = "El tipo de movimiento es obligatorio")
        CashMovementType movementType,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String description
) {}