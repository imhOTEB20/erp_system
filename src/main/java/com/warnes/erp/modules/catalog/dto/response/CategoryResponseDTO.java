package com.warnes.erp.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Datos maestros de una categoría/familia de productos")
public record CategoryResponseDTO(
        @Schema(description = "Identificador público de la categoría", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uuid,

        @Schema(description = "Nombre comercial", example = "Filtros de Aceite")
        String name,

        @Schema(description = "Estado actual", example = "true")
        boolean active
) {}