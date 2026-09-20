package com.warnes.erp.modules.inventory.dto.response;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record TransferResponseDTO(
        UUID uuid,
        String trackingCode,
        UUID originWarehouseUuid,
        String originWarehouseName,
        UUID targetWarehouseUuid,
        String targetWarehouseName,
        String status,
        ZonedDateTime createdAt,
        List<TransferItemResponseDTO> items
) {}