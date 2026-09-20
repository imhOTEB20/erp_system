package com.warnes.erp.modules.purchases.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseResponseDTO(
        UUID uuid,
        UUID supplierUuid,
        String supplierName,
        UUID destinationWarehouseUuid,
        String destinationWarehouseName,
        String invoiceNumber,
        BigDecimal totalAmount,
        ZonedDateTime createdAt,
        List<PurchaseItemResponseDTO> items,
        List<PurchaseTaxResponseDTO> taxes
) {}