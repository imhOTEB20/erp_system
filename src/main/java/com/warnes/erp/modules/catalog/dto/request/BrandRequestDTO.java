package com.warnes.erp.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para la creación o modificación de una marca en el catálogo")
public record BrandRequestDTO(
        @Schema(description = "Nombre comercial de la marca", example = "Bosch")
        @NotBlank(message = "El nombre de la marca es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String name,

        @Schema(description = "Estado de visualización en el sistema", example = "true")
        @NotNull(message = "El estado (activo/inactivo) es obligatorio")
        Boolean active
) {}