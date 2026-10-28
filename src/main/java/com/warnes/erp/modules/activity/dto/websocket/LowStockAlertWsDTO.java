package com.warnes.erp.modules.activity.dto.websocket;

import com.warnes.erp.modules.inventory.entity.OperationSource;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record LowStockAlertWsDTO(
        String eventType, // "PRODUCT_LOW_STOCK_ALERT"
        UUID warehouseUuid,
        String warehouseName,
        UUID productUuid,
        String productSku,
        String productName,
        BigDecimal previousQuantity,
        BigDecimal currentQuantity,
        BigDecimal minStock,
        OperationSource operationSource, // "SALE" o "TRANSFER"
        String message,
        ZonedDateTime timestamp
) {}