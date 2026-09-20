package com.warnes.erp.modules.sales.dto.response;

import com.warnes.erp.modules.sales.entity.LedgerMovementType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record CustomerLedgerResponseDTO(
        LedgerMovementType movementType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        ZonedDateTime createdAt,
        UUID saleUuid,          // Referencia a la venta si existe
        String invoiceNumber    // Para facilitar la lectura al cliente
) {}