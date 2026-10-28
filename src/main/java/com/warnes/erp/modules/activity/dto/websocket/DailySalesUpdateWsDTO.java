package com.warnes.erp.modules.activity.dto.websocket;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record DailySalesUpdateWsDTO(
        String eventType, // "SALE_COMPLETED" o "SALE_CANCELLED_OR_REFUNDED"
        UUID warehouseUuid,
        String warehouseName,
        UUID saleUuid,
        String invoiceNumber,
        BigDecimal operationAmount,
        long warehouseSalesCountToday,
        BigDecimal warehouseSalesTotalToday,
        long globalSalesCountToday,
        BigDecimal globalSalesTotalToday,
        ZonedDateTime timestamp
) {}