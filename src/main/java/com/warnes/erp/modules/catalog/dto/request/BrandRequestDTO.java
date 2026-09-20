package com.warnes.erp.modules.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BrandRequestDTO(
        @NotBlank(message = "El nombre de la marca es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String name,

        @NotNull(message = "El estado (activo/inactivo) es obligatorio")
        Boolean active
) {}