package com.warnes.erp.modules.purchases.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseItemResponseDTO(
        UUID productUuid,
        String productSku,
        String productName,
        int quantity,
        BigDecimal unitCost
) {}