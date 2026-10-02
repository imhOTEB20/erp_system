package com.warnes.erp.modules.cash.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para la edición de una estación de caja física")
public record CashRegisterUpdateRequestDTO(
        @Schema(description = "Nombre identificador de la caja en el local", example = "Caja Mostrador 1")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String name,

        @Schema(description = "Caja disponible / Caja inhabilitada", example = "true -> caja disponible para registrar ventas.")
        Boolean active
) {}