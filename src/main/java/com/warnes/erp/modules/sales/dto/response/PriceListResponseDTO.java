package com.warnes.erp.modules.sales.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PriceListResponseDTO(
        UUID uuid,
        String name,
        BigDecimal marginPercentage
) {}