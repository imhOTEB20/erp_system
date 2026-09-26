package com.warnes.erp.modules.cash.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Payload para la creación de una estación de caja física")
public record CashRegisterRequestDTO(
        @Schema(description = "Identificador de la sucursal a la que pertenece la caja")
        @NotNull(message = "El identificador de la sucursal es obligatorio")
        UUID warehouseUuid,

        @Schema(description = "Nombre identificador de la caja en el local", example = "Caja Mostrador 1")
        @NotBlank(message = "El nombre de la caja es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String name
) {}