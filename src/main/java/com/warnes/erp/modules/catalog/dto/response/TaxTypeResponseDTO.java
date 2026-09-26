package com.warnes.erp.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Datos maestros de un tipo de impuesto o alícuota")
public record TaxTypeResponseDTO(
        @Schema(description = "Identificador público del impuesto", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "Nombre oficial del impuesto", example = "IVA Responsable Inscripto")
        String name,

        @Schema(description = "Tasa nominal", example = "21.0000")
        BigDecimal defaultRate,

        @Schema(description = "Estado actual", example = "true")
        boolean active
) {}