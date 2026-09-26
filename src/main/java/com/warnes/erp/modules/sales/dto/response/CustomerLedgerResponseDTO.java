package com.warnes.erp.modules.sales.dto.response;

import com.warnes.erp.modules.sales.entity.LedgerMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Registro contable individual del Libro Mayor (Cuenta Corriente) de un cliente")
public record CustomerLedgerResponseDTO(
        @Schema(description = "Naturaleza de la transacción", example = "SALE_ON_CREDIT")
        LedgerMovementType movementType,

        @Schema(description = "Monto involucrado en la transacción", example = "150000.00")
        BigDecimal amount,

        @Schema(description = "Saldo final de la cuenta corriente tras la operación (Negativo implica deuda a favor del local)", example = "-150000.00")
        BigDecimal balanceAfter,

        @Schema(description = "Fecha de la transacción", example = "2026-09-21T16:45:00Z")
        ZonedDateTime createdAt,

        @Schema(description = "UUID de la factura de venta (si la transacción originó de una venta)", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID saleUuid,

        @Schema(description = "Número visible de la factura para auditoría rápida", example = "B-0001-0000123")
        String invoiceNumber
) {}