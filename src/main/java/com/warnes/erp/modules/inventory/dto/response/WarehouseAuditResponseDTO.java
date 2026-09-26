package com.warnes.erp.modules.inventory.dto.response;

import com.warnes.erp.modules.inventory.entity.WarehouseType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Datos administrativos y de auditoría de un Almacén (incluye trazabilidad de borrado)")
public record WarehouseAuditResponseDTO(
        @Schema(description = "Identificador público del almacén")
        UUID uuid,

        @Schema(description = "Código corto logístico")
        String code,

        @Schema(description = "Nombre de la sucursal")
        String name,

        @Schema(description = "Tipo de almacen")
        WarehouseType type,

        @Schema(description = "Indica si el almacén está activo operativamente")
        boolean active,

        @Schema(description = "Fecha y hora de creación del registro")
        ZonedDateTime createdAt,

        @Schema(description = "Fecha y hora de la última modificación")
        ZonedDateTime updatedAt,

        @Schema(description = "Fecha y hora en la que fue eliminado lógicamente (Soft Delete)")
        ZonedDateTime deletedAt
) {}