package com.warnes.erp.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Detalle de una equivalencia o cross-reference de producto")
public record ProductCrossReferenceResponseDTO(
        @Schema(description = "UUID interno de la referencia", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "Marca de competencia", example = "Fram")
        String externalBrand,

        @Schema(description = "SKU de la competencia equivalente a nuestro repuesto", example = "PH2870A")
        String externalSku
) {}