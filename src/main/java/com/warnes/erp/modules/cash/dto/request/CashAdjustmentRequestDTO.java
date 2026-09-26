package com.warnes.erp.modules.cash.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Payload para registrar un ajuste especial en la caja (Sangría o Ingreso de Sencillo)")
public record CashAdjustmentRequestDTO(
        @Schema(description = "Monto exacto del ajuste", example = "15000.00")
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @Schema(description = "Justificación del movimiento", example = "Retiro de seguridad por exceso de billetes grandes")
        @NotBlank(message = "Debe justificar el movimiento")
        String description
) {}