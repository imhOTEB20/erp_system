package com.warnes.erp.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para la creación o modificación de categorías de productos")
public record CategoryRequestDTO(
        @Schema(description = "Nombre de la familia o categoría", example = "Filtros de Aceite")
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String name,

        @Schema(description = "Descripcion breve de la categoria")
        @Size(max = 100, message = "La descripcion no puede exceder los 100 caracteres")
        String description,

        @Schema(description = "Estado de visualización en el sistema", example = "true")
        @NotNull(message = "El estado (activo/inactivo) es obligatorio")
        Boolean active
) {}