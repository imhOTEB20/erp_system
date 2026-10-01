package com.warnes.erp.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Datos minimos para control de stock por sucursal.")
public record ProductSimpleResponseDTO(
        @Schema(description = "Uuid del producto.")
        UUID uuid,

        @Schema(description = "Nombre del producto.")
        String name
){
}
