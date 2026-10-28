package com.warnes.erp.modules.sales.dto.response;

import java.math.BigDecimal;

public record ProfitMarginResponseDTO(
        String name,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal profitAmount,
        BigDecimal marginPercentage
) {
}
