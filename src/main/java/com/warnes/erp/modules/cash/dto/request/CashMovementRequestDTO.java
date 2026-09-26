package com.warnes.erp.modules.cash.dto.request;

import com.warnes.erp.modules.cash.entity.CashMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Payload para asentar un movimiento manual en el turno de caja activo")
public record CashMovementRequestDTO(
        @Schema(description = "Categoría del movimiento (Ingreso, Egreso, etc.)", example = "EXPENSE")
        @NotNull(message = "El tipo de movimiento es obligatorio")
        CashMovementType movementType,

        @Schema(description = "Monto del movimiento", example = "2500.50")
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @Schema(description = "Motivo o descripción detallada", example = "Pago a proveedor de insumos de limpieza")
        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String description
) {}