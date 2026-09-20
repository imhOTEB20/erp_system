package com.warnes.erp.modules.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferItemRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @Min(value = 1, message = "La cantidad a transferir debe ser mayor a 0")
        BigDecimal quantity
) {}