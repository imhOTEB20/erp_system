package com.warnes.erp.modules.catalog.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxTypeResponseDTO(
        UUID uuid,
        String name,
        BigDecimal defaultRate,
        boolean active
) {}