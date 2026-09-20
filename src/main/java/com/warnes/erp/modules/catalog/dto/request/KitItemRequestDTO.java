package com.warnes.erp.modules.catalog.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record KitItemRequestDTO(
        @NotNull(message = "El UUID del producto componente es obligatorio")
        UUID childProductUuid,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity
) {}