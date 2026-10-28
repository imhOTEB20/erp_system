package com.warnes.erp.modules.activity.service;

import com.warnes.erp.modules.activity.dto.response.ActivityLogResponseDTO;
import com.warnes.erp.modules.activity.dto.response.AdminDashboardSummaryDTO;
import com.warnes.erp.modules.activity.entity.ActivityType;
import com.warnes.erp.modules.cash.entity.CashRegister;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.sales.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface ActivityLogService {

    void recordDomainActivity(
            ActivityType type,
            String module,
            String title,
            String description,
            UUID referenceUuid,
            String referenceCode,
            BigDecimal amount,
            Warehouse originWarehouse,
            Warehouse targetWarehouse,
            Customer customer,
            CashRegister cashRegister
    );

    Page<ActivityLogResponseDTO> getRecentActivities(
            UUID warehouseUuid,
            UUID customerUuid,
            UUID cashRegisterUuid,
            String module,
            ActivityType activityType,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    );

    AdminDashboardSummaryDTO getAdminDashboardSummary(UUID warehouseUuid);
}