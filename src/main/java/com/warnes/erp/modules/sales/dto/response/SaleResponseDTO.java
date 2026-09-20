package com.warnes.erp.modules.sales.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record SaleResponseDTO(
        UUID uuid,
        String invoiceNumber,
        UUID cashShiftUuid,
        UUID customerUuid,
        String customerName,
        String status,
        BigDecimal totalAmount,
        ZonedDateTime createdAt,
        List<SaleItemResponseDTO> items
) {}