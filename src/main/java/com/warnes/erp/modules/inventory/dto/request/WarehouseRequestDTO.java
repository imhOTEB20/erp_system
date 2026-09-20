package com.warnes.erp.modules.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseRequestDTO(
        @NotBlank(message = "El código de almacén es obligatorio")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        String code,

        @NotBlank(message = "El nombre del almacén es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name
) {}