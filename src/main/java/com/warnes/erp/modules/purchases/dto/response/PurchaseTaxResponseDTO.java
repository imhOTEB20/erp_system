package com.warnes.erp.modules.purchases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Detalle de un impuesto procesado en una factura de compra")
public record PurchaseTaxResponseDTO(
        @Schema(description = "UUID del tipo de impuesto", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID taxTypeUuid,

        @Schema(description = "Nombre del impuesto", example = "IVA 21%")
        String taxTypeName,

        @Schema(description = "Monto monetario del impuesto", example = "152250.00")
        BigDecimal taxAmount
) {}