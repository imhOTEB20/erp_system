package com.warnes.erp.modules.cash.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CloseShiftRequestDTO(
        @NotNull(message = "Debe declarar el monto físico contado en efectivo")
        @DecimalMin(value = "0.00", message = "El monto declarado no puede ser negativo")
        BigDecimal declaredCash,

        String observations
) {}