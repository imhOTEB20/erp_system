package com.warnes.erp.modules.cash.dto.response;

import com.warnes.erp.modules.cash.entity.CashMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Schema(description = "Detalle de un movimiento individual de efectivo dentro de un turno")
public record CashMovementResponseDTO(
        @Schema(description = "Tipo de movimiento", example = "SYSTEM_ADJUSTMENT")
        CashMovementType movementType,
        @Schema(description = "Monto exacto involucrado", example = "150.50")
        BigDecimal amount,
        @Schema(description = "Motivo detallado", example = "Anulación de descuadre original")
        String description,
        @Schema(description = "Nombre del operador o administrador responsable", example = "Cristian Gerónimo")
        String userName,
        @Schema(description = "Fecha y hora exacta en la que se generó", example = "2026-10-02T08:00:00Z")
        ZonedDateTime createdAt
) {}