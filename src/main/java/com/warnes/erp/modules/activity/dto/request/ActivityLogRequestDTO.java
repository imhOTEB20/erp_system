package com.warnes.erp.modules.activity.dto.request;

import com.warnes.erp.modules.activity.entity.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ActivityLogRequestDTO(
        @NotNull ActivityType activityType,
        @NotBlank String module,
        @NotBlank String title,
        @NotBlank String description,
        UUID referenceUuid,
        String referenceCode,
        BigDecimal amount,
        UUID warehouseUuid,
        UUID targetWarehouseUuid
) {}