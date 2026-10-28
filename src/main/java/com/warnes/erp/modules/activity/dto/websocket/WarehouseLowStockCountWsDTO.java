package com.warnes.erp.modules.activity.dto.websocket;

import lombok.Builder;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record WarehouseLowStockCountWsDTO(
        String eventType, // "LOW_STOCK_COUNT_UPDATED"
        UUID warehouseUuid,
        String warehouseName,
        long totalLowStockProductsInWarehouse,
        long totalZeroStockProductsInWarehouse,
        long globalLowStockProducts,
        ZonedDateTime timestamp
) {}