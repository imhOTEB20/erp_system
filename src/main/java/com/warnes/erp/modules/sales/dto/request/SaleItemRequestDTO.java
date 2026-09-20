package com.warnes.erp.modules.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity
) {}