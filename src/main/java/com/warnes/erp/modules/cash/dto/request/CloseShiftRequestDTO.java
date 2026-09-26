package com.warnes.erp.modules.cash.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Payload para ejecutar el 'Cierre Ciego' del turno de caja actual")
public record CloseShiftRequestDTO(
        @Schema(description = "Suma total del efectivo físico contado por el cajero en la gaveta", example = "142500.00")
        @NotNull(message = "Debe declarar el monto físico contado en efectivo")
        @DecimalMin(value = "0.00", message = "El monto declarado no puede ser negativo")
        BigDecimal declaredCash,

        @Schema(description = "Anotaciones adicionales del cajero sobre el cierre", example = "Se me cayó una moneda de $50 y no la encontré")
        String observations
) {}