package com.warnes.erp.modules.inventory.dto.request;

import com.warnes.erp.modules.inventory.entity.WarehouseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para la creación o modificación de un Almacén / Sucursal física")
public record WarehouseRequestDTO(
        @Schema(description = "Código corto de identificación del almacén", example = "SUC-CARRIL")
        @NotBlank(message = "El código de almacén es obligatorio")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        String code,

        @Schema(description = "Nombre descriptivo de la sucursal", example = "Sucursal El Carril Principal")
        @NotBlank(message = "El nombre del almacén es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Schema(description = "Tipo de almacén (ej: STORE, DEPOT). Si se omite, asume STORE por defecto.", example = "STORE", nullable = true)
        WarehouseType type
) {
        public WarehouseRequestDTO {
                if (type == null) {
                        type = WarehouseType.STORE;
                }
        }
}