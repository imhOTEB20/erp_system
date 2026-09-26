package com.warnes.erp.modules.inventory.dto.request;

import com.warnes.erp.modules.inventory.entity.WarehouseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para la actualización parcial de un Almacén (solo se envían los campos a modificar)")
public record WarehouseUpdateRequestDTO(
        @Schema(description = "Nuevo código corto", example = "DEP-CARRIL")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        String code,

        @Schema(description = "Nuevo nombre descriptivo", example = "Depósito Central El Carril")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Schema(description = "Nuevo tipo de almacén", example = "MAIN")
        WarehouseType type,

        @Schema(description = "Modificar estado operativo", example = "true")
        Boolean active
) {}