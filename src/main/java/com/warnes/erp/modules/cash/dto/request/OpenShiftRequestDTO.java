package com.warnes.erp.modules.cash.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OpenShiftRequestDTO(
        @NotNull(message = "La caja es obligatoria")
        UUID cashRegisterUuid,

        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
        BigDecimal openingBalance
) {}