package com.warnes.erp.modules.purchases.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseTaxResponseDTO(
        UUID taxTypeUuid,
        String taxTypeName,
        BigDecimal taxAmount
) {}