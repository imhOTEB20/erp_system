package com.warnes.erp.modules.sales.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemResponseDTO(
        UUID productUuid,
        String productSku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}