package com.warnes.erp.modules.cash.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Detalle completo del estado de un turno de caja")
public record CashShiftResponseDTO(
        @Schema(description = "Identificador del turno", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uuid,

        @Schema(description = "Identificador de la caja vinculada", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID cashRegisterUuid,

        @Schema(description = "Nombre de la caja vinculada", example = "Caja Mostrador 1")
        String cashRegisterName,

        @Schema(description = "Identificador del operador/cajero", example = "987e6543-e21b-12d3-a456-426614174000")
        UUID userUuid,

        @Schema(description = "Nombre de usuario del operador", example = "jdoe")
        String username,

        @Schema(description = "Estado actual del turno", example = "OPEN")
        String status,

        @Schema(description = "Saldo físico con el que inició el turno", example = "10000.00")
        BigDecimal openingBalance,

        @Schema(description = "Saldo que el sistema calculó que debería haber (Solo visible para ADMIN hasta el cierre)", example = "45500.50")
        BigDecimal expectedBalance,

        @Schema(description = "Saldo físico contado por el cajero al finalizar (Cierre ciego)", example = "45500.00")
        BigDecimal declaredBalance,

        @Schema(description = "Descuadre final. Negativo es faltante, positivo es sobrante.", example = "-0.50")
        BigDecimal discrepancy,

        @Schema(description = "Fecha y hora de inicio de operaciones", example = "2026-09-21T08:00:00Z")
        ZonedDateTime openedAt,

        @Schema(description = "Fecha y hora de finalización del turno", example = "2026-09-21T18:00:00Z")
        ZonedDateTime endedAt,

        @Schema(description = "Observaciones generales del turno", example = "Todo en orden")
        String observations
) {}