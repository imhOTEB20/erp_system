package com.warnes.erp.modules.cash.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload para dar inicio a un nuevo turno operativo de caja")
public record OpenShiftRequestDTO(
        @Schema(description = "UUID de la estación de caja física a utilizar", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "La caja es obligatoria")
        UUID cashRegisterUuid,

        @Schema(description = "Fondo de cambio inicial depositado en la gaveta", example = "10000.00")
        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
        BigDecimal openingBalance
) {}