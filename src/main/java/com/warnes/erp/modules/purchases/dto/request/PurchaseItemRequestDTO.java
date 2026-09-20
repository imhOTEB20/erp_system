package com.warnes.erp.modules.purchases.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseItemRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity,

        @NotNull(message = "El costo unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
        BigDecimal unitCost
) {}