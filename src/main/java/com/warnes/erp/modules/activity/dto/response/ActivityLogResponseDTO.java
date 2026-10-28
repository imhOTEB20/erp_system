package com.warnes.erp.modules.activity.dto.response;

import com.warnes.erp.modules.activity.entity.ActivityType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record ActivityLogResponseDTO(
        UUID uuid,
        ActivityType activityType,
        String module,
        String title,
        String description,
        UUID referenceUuid,
        String referenceCode,
        BigDecimal amount,
        UUID warehouseUuid,
        String warehouseName,
        UUID targetWarehouseUuid,
        String targetWarehouseName,
        UUID customerUuid,
        String customerName,
        UUID cashRegisterUuid,
        String cashRegisterName,
        String performedBy,
        ZonedDateTime createdAt
) {}