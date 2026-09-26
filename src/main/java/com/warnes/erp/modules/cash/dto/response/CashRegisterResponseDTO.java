package com.warnes.erp.modules.cash.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Respuesta con los datos maestros de una caja registradora")
public record CashRegisterResponseDTO(
        @Schema(description = "Identificador público de la caja")
        UUID uuid,

        @Schema(description = "Identificador público de la sucursal")
        UUID warehouseUuid,

        @Schema(description = "Nombre de la caja")
        String name,

        @Schema(description = "Indica si la caja está habilitada para operar")
        boolean active
) {}