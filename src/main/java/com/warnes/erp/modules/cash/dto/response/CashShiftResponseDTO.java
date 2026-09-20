package com.warnes.erp.modules.cash.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record CashShiftResponseDTO(
        UUID uuid,
        UUID cashRegisterUuid,
        String cashRegisterName, // Agregado para el Mapper
        UUID userUuid,           // Agregado para el Mapper
        String username,         // Agregado para el Mapper
        String status,
        BigDecimal openingBalance, // Corregido para coincidir con la entidad
        BigDecimal expectedBalance,
        BigDecimal declaredBalance,
        BigDecimal discrepancy,
        ZonedDateTime openedAt,    // Corregido para coincidir con la entidad
        ZonedDateTime endedAt,
        String observations
) {}