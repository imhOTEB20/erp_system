package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Schema(description = "Totales de ventas por periodo")
public record SaleByPeriodResponseDTO(
        ZonedDateTime startPeriod,
        ZonedDateTime endPeriod,
        BigDecimal totalAmount,
        BigDecimal quantitySold
) {
}
