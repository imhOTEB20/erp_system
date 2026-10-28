package com.warnes.erp.modules.activity.service.impl;

import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.activity.dto.response.ActivityLogResponseDTO;
import com.warnes.erp.modules.activity.dto.response.AdminDashboardSummaryDTO;
import com.warnes.erp.modules.activity.entity.ActivityLog;
import com.warnes.erp.modules.activity.entity.ActivityType;
import com.warnes.erp.modules.activity.mapper.ActivityLogMapper;
import com.warnes.erp.modules.activity.repository.ActivityLogRepository;
import com.warnes.erp.modules.activity.repository.ActivityLogSpecification;
import com.warnes.erp.modules.activity.service.ActivityLogService;
import com.warnes.erp.modules.cash.entity.CashRegister;
import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.cash.repository.CashShiftRepository;
import com.warnes.erp.modules.cash.repository.CashShiftSpecification;
import com.warnes.erp.modules.inventory.entity.TransferStatus;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.repository.*;
import com.warnes.erp.modules.sales.entity.Customer;
import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.repository.SaleRepository;
import com.warnes.erp.modules.sales.repository.SaleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    private final StockRepository stockRepository;
    private final TransferRepository transferRepository;
    private final CashShiftRepository cashShiftRepository;
    private final SaleRepository saleRepository;
    private final WarehouseRepository warehouseRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void recordDomainActivity(
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
    ) {
        ActivityLog log = new ActivityLog();
        log.setActivityType(type);
        log.setModule(module);
        log.setTitle(title);
        log.setDescription(description);
        log.setReferenceUuid(referenceUuid);
        log.setReferenceCode(referenceCode);
        log.setAmount(amount);
        log.setWarehouse(originWarehouse);
        log.setTargetWarehouse(targetWarehouse);
        log.setCustomer(customer);
        log.setCashRegister(cashRegister);
        log.setPerformedBy(resolveCurrentUsername());

        ActivityLog saved = activityLogRepository.save(log);
        ActivityLogResponseDTO responseDTO = activityLogMapper.toResponseDTO(saved);

        // Emitir nueva actividad en vivo al feed de Administradores y de la Sucursal
        messagingTemplate.convertAndSend("/topic/admin/activities", responseDTO);
        if (originWarehouse != null) {
            messagingTemplate.convertAndSend(
                    "/topic/warehouse/" + originWarehouse.getUuid() + "/activities", responseDTO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponseDTO> getRecentActivities(
            UUID warehouseUuid,
            UUID customerUuid,
            UUID cashRegisterUuid,
            String module,
            ActivityType activityType,
            ZonedDateTime from,
            ZonedDateTime to,
            Pageable pageable
    ) {
        var spec = ActivityLogSpecification.filter(
                warehouseUuid, customerUuid, cashRegisterUuid, module, activityType, from, to
        );
        return activityLogRepository.findAll(spec, pageable)
                .map(activityLogMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryDTO getAdminDashboardSummary(UUID warehouseUuid) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        Warehouse warehouse = warehouseRepository.findByUuid(warehouseUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));


        // Ventas completadas hoy usando Specification (refleja ventas cobradas y descuenta las canceladas)
        List<Sale> todaySales = saleRepository.findAll(
                SaleSpecification.completedSalesInDateRange(warehouseUuid, startOfDay, now)
        );

        BigDecimal salesTotalToday = todaySales.stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ActivityLog> todayPurchases = activityLogRepository.findAll(
                ActivityLogSpecification.filter(
                        warehouseUuid, null, null, "PURCHASES", ActivityType.PURCHASE_REGISTERED, startOfDay, now)
        );

        BigDecimal purchasesTotalToday = todayPurchases.stream()
                .map(a -> a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        //Todo calculado con Specification sin @Query
        long lowStockCount = stockRepository.count(StockSpecification.belowOrEqualMinStock(warehouseUuid));
        long zeroStockCount = stockRepository.count(StockSpecification.zeroStock(warehouseUuid));
        long activeTransfers = transferRepository.count(
                TransferSpecification.hasStatusAndWarehouse(TransferStatus.PENDING, warehouseUuid));

        Specification<CashShift> filter = CashShiftSpecification.onlyOpen()
                .and(CashShiftSpecification.inThisWarehouse(warehouseUuid));
        long openShifts = cashShiftRepository.count(filter);


        List<ActivityLogResponseDTO> latestActivities = activityLogRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(activityLogMapper::toResponseDTO)
                .toList();

        return AdminDashboardSummaryDTO.builder()
                .salesCountToday(todaySales.size())
                .salesTotalToday(salesTotalToday)
                .purchasesCountToday(todayPurchases.size())
                .purchasesTotalToday(purchasesTotalToday)
                .pendingOrInTransitTransfers(activeTransfers)
                .openCashShiftsCount(openShifts)
                .lowStockAlertsCount(lowStockCount)
                .zeroStockAlertsCount(zeroStockCount)
                .latestActivities(latestActivities)
                .build();

    }

    private String resolveCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }
}