package com.warnes.erp.modules.activity.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AdminDashboardSummaryDTO(
        long salesCountToday,
        BigDecimal salesTotalToday,
        long purchasesCountToday,
        BigDecimal purchasesTotalToday,
        long pendingOrInTransitTransfers,
        long openCashShiftsCount,
        long lowStockAlertsCount,
        long zeroStockAlertsCount,
        List<ActivityLogResponseDTO> latestActivities
) {}