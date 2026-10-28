package com.warnes.erp.modules.activity.dto.websocket;

import com.warnes.erp.modules.inventory.entity.TransferStatus;
import lombok.Builder;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record PendingTransfersUpdateWsDTO(
        String eventType, // "TRANSFER_CREATED", "TRANSFER_CONFIRMED", "TRANSFER_CANCELLED"
        UUID transferUuid,
        TransferStatus currentStatus,
        UUID sourceWarehouseUuid,
        String sourceWarehouseName,
        UUID destinationWarehouseUuid,
        String destinationWarehouseName,
        long totalPendingTransfersGlobal,
        long sourceWarehousePendingTransfers,
        long destinationWarehousePendingTransfers,
        ZonedDateTime timestamp
) {}