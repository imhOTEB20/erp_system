package com.warnes.erp.modules.sales.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerResponseDTO(
        UUID uuid,
        String docNumber,
        String businessName,
        BigDecimal currentBalance,
        UUID priceListUuid,
        String priceListName
) {}