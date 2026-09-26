package com.warnes.erp.modules.purchases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload para registrar un impuesto o percepción cobrada por el proveedor en la factura")
public record PurchaseTaxRequestDTO(
        @Schema(description = "UUID del tipo de impuesto (IVA, Percepción IIBB, etc.)", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "El UUID del tipo de impuesto es obligatorio")
        UUID taxTypeUuid,

        @Schema(description = "Monto monetario exacto del impuesto", example = "3045.50")
        @NotNull(message = "El monto del impuesto es obligatorio")
        @DecimalMin(value = "0.00", message = "El monto del impuesto no puede ser negativo")
        BigDecimal taxAmount
) {}