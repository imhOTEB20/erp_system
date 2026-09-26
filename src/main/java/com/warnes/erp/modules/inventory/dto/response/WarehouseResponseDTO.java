package com.warnes.erp.modules.inventory.dto.response;

import com.warnes.erp.modules.inventory.entity.WarehouseType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Datos maestros de un Almacén o Sucursal física")
public record WarehouseResponseDTO(
        @Schema(description = "Identificador público del almacén", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "Código corto logístico", example = "SUC-CARRIL")
        String code,

        @Schema(description = "Nombre de la sucursal", example = "Sucursal El Carril Principal")
        String name,

        @Schema(description = "Indica si el almacén está activo y puede operar", example = "true")
        boolean active,

        @Schema(description = "Tipo de almacen", example = "DEPOT")
        WarehouseType type
) {}