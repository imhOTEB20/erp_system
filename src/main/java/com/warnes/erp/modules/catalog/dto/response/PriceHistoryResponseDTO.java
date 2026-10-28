package com.warnes.erp.modules.catalog.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PriceHistoryResponseDTO(
        ZonedDateTime changedAt,
        BigDecimal costPrice,
        BigDecimal salePrice
) {
}
