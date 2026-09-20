package com.warnes.erp.modules.inventory.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record StockMovementResponseDTO(
        UUID uuid,
        String productSku,
        String productName,
        String warehouseName,
        String userName,
        BigDecimal quantity,
        BigDecimal balanceAfter,
        String movementType,
        UUID referenceDocument,
        ZonedDateTime createdAt
) {}