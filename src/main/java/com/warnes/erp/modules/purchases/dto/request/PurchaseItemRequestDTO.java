package com.warnes.erp.modules.purchases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload del detalle de un producto ingresado en una compra")
public record PurchaseItemRequestDTO(
        @Schema(description = "UUID del producto comprado", example = "770e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El producto es obligatorio")
        UUID productUuid,

        @Schema(description = "Cantidad ingresada al almacén", example = "50.00")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity,

        @Schema(description = "Costo unitario pagado al proveedor (actualizará el costo base del catálogo)", example = "14500.00")
        @NotNull(message = "El costo unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
        BigDecimal unitCost
) {}