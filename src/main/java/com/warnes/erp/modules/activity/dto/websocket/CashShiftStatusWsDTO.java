package com.warnes.erp.modules.activity.dto.websocket;

import com.warnes.erp.modules.cash.entity.ShiftStatus;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record CashShiftStatusWsDTO(
        String eventType, // "CASH_SHIFT_OPENED" o "CASH_SHIFT_CLOSED"
        UUID shiftUuid,
        ShiftStatus status,
        UUID cashRegisterUuid,
        String cashRegisterName,
        UUID warehouseUuid,
        String warehouseName,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        long openShiftsInWarehouse,
        String performedBy,
        String message,
        ZonedDateTime timestamp
) {}