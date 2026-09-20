package com.warnes.erp.modules.catalog.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record KitItemResponseDTO(
        UUID childProductUuid,
        String childProductSku,
        String childProductName,
        BigDecimal quantity,
        BigDecimal unitCost, // Costo individual del componente
        BigDecimal subtotalCost // Costo total (unitCost * quantity)
) {}