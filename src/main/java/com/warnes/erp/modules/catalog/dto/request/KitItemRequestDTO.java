package com.warnes.erp.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload para definir la relación de un producto individual dentro de un producto Kit/Combo")
public record KitItemRequestDTO(
        @Schema(description = "UUID del producto unitario que compondrá el kit", example = "770e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El UUID del producto componente es obligatorio")
        UUID childProductUuid,

        @Schema(description = "Cantidad del componente necesaria para armar un (1) kit", example = "4.00")
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity
) {}