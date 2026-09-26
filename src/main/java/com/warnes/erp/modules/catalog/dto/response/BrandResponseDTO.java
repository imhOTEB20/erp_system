package com.warnes.erp.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Datos maestros de una marca")
public record BrandResponseDTO(
        @Schema(description = "Identificador público de la marca", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "Nombre de la marca", example = "Bosch")
        String name,

        @Schema(description = "Estado actual", example = "true")
        boolean active
) {}