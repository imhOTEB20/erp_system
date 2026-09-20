package com.warnes.erp.modules.purchases.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseTaxRequestDTO(
        @NotNull(message = "El UUID del tipo de impuesto es obligatorio")
        UUID taxTypeUuid,

        @NotNull(message = "El monto del impuesto es obligatorio")
        @DecimalMin(value = "0.00", message = "El monto del impuesto no puede ser negativo")
        BigDecimal taxAmount
) {}