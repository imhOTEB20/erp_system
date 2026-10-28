package com.warnes.erp.modules.activity.service;

import com.warnes.erp.modules.activity.dto.websocket.*;
import com.warnes.erp.modules.activity.event.ErpRealtimeEvents.*;
import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.cash.repository.CashShiftRepository;
import com.warnes.erp.modules.cash.repository.CashShiftSpecification;
import com.warnes.erp.modules.inventory.entity.OperationSource;
import com.warnes.erp.modules.inventory.entity.TransferStatus;
import com.warnes.erp.modules.inventory.repository.StockRepository;
import com.warnes.erp.modules.inventory.repository.StockSpecification;
import com.warnes.erp.modules.inventory.repository.TransferRepository;
import com.warnes.erp.modules.inventory.repository.TransferSpecification;
import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.repository.SaleRepository;
import com.warnes.erp.modules.sales.repository.SaleSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeNotificationListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final StockRepository stockRepository;
    private final TransferRepository transferRepository;
    private final SaleRepository saleRepository;
    private final CashShiftRepository cashShiftRepository;

    /**
     * Cubre las Acciones 1 y 2:
     * 1) Si es VENTA o TRANSFERENCIA y el nuevo stock <= minStock, envía alerta detallada del producto.
     * 2) Si el stock cruzó el umbral del mínimo (entró en bajo stock: antes > min y ahora <= min,
     *    o salió de bajo stock: antes <= min y ahora > min, o es inicialización en bajo stock),
     *    actualiza el contador total de productos con stock bajo en la sucursal Z.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onStockChanged(StockChangedDomainEvent event) {
        boolean isNowLowStock = event.newQuantity().compareTo(event.minStock()) <= 0;
        boolean wasLowStockBefore = event.previousQuantity() != null
                && event.previousQuantity().compareTo(event.minStock()) <= 0;

        // ACCIÓN 1: Cuando se vende o transfiere y el stock desciende al mínimo o por debajo
        boolean isSaleOrTransfer = OperationSource.SALE.equals(event.operationSource())
                || OperationSource.TRANSFER.equals(event.operationSource());
        boolean stockDecreased = event.previousQuantity() == null
                || event.newQuantity().compareTo(event.previousQuantity()) < 0;

        if (isSaleOrTransfer && stockDecreased && isNowLowStock) {
            LowStockAlertWsDTO alertDto = LowStockAlertWsDTO.builder()
                    .eventType("PRODUCT_LOW_STOCK_ALERT")
                    .warehouseUuid(event.warehouseUuid())
                    .warehouseName(event.warehouseName())
                    .productUuid(event.productUuid())
                    .productSku(event.productSku())
                    .productName(event.productName())
                    .previousQuantity(event.previousQuantity())
                    .currentQuantity(event.newQuantity())
                    .minStock(event.minStock())
                    .operationSource(event.operationSource())
                    .message(String.format(
                            "Alerta de Stock en %s: '%s' (SKU: %s) quedó con stock %s (Mínimo establecido: %s)",
                            event.warehouseName(), event.productName(), event.productSku(),
                            event.newQuantity(), event.minStock()))
                    .timestamp(ZonedDateTime.now())
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/warehouse/" + event.warehouseUuid() + "/low-stock-alerts", alertDto);
            messagingTemplate.convertAndSend("/topic/admin/low-stock-alerts", alertDto);
        }

        // ACCIÓN 2: Cuando se altera algún stock (venta, compra, ajuste, etc.) y entra o sale del mínimo establecido
        boolean thresholdCrossedOrReached = (event.previousQuantity() == null && isNowLowStock)
                || (wasLowStockBefore != isNowLowStock)
                || isNowLowStock;

        if (thresholdCrossedOrReached) {
            long warehouseLowStockCount = stockRepository.count(
                    StockSpecification.belowOrEqualMinStock(event.warehouseUuid()));
            long warehouseZeroStockCount = stockRepository.count(
                    StockSpecification.zeroStock(event.warehouseUuid()));
            long globalLowStockCount = stockRepository.count(
                    StockSpecification.belowOrEqualMinStock(null));

            WarehouseLowStockCountWsDTO countDto = WarehouseLowStockCountWsDTO.builder()
                    .eventType("LOW_STOCK_COUNT_UPDATED")
                    .warehouseUuid(event.warehouseUuid())
                    .warehouseName(event.warehouseName())
                    .totalLowStockProductsInWarehouse(warehouseLowStockCount)
                    .totalZeroStockProductsInWarehouse(warehouseZeroStockCount)
                    .globalLowStockProducts(globalLowStockCount)
                    .timestamp(ZonedDateTime.now())
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/warehouse/" + event.warehouseUuid() + "/low-stock-count", countDto);
            messagingTemplate.convertAndSend("/topic/admin/low-stock-count", countDto);
        }
    }

    /**
     * Cubre la Acción 3: Apertura o Cierre de turno en la sucursal Z.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onCashShiftChanged(CashShiftDomainEvent event) {
        Specification<CashShift> filter = CashShiftSpecification
                .onlyOpen()
                .and(CashShiftSpecification.inThisWarehouse(event.warehouseUuid()));
        long openShiftsInWarehouse = cashShiftRepository.count(filter);

        String eventType = event.status() == ShiftStatus.OPEN ? "CASH_SHIFT_OPENED" : "CASH_SHIFT_CLOSED";
        String actionText = event.status() == ShiftStatus.OPEN ? "abrió" : "cerró";

        CashShiftStatusWsDTO dto = CashShiftStatusWsDTO.builder()
                .eventType(eventType)
                .shiftUuid(event.shiftUuid())
                .status(event.status())
                .cashRegisterUuid(event.cashRegisterUuid())
                .cashRegisterName(event.cashRegisterName())
                .warehouseUuid(event.warehouseUuid())
                .warehouseName(event.warehouseName())
                .openingBalance(event.openingBalance())
                .closingBalance(event.closingBalance())
                .openShiftsInWarehouse(openShiftsInWarehouse)
                .performedBy(event.performedBy())
                .message(String.format("Se %s el turno en caja '%s' (%s)",
                        actionText, event.cashRegisterName(), event.warehouseName()))
                .timestamp(ZonedDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/warehouse/" + event.warehouseUuid() + "/cash-shifts", dto);
        messagingTemplate.convertAndSend("/topic/admin/cash-shifts", dto);
    }

    /**
     * Cubre la Acción 4: Si se realiza una venta o se cancela/devuelve una venta ya cobrada,
     * actualiza el total de ventas del día.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onDailySalesChanged(DailySalesDomainEvent event) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId);
        ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        // Ventas completadas hoy en la sucursal Z (usando Specification)
        List<Sale> warehouseSalesToday = saleRepository.findAll(
                SaleSpecification.completedSalesInDateRange(event.warehouseUuid(), startOfDay, endOfDay));
        BigDecimal warehouseTotal = warehouseSalesToday.stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ventas completadas hoy en todo el ERP (usando Specification)
        List<Sale> globalSalesToday = saleRepository.findAll(
                SaleSpecification.completedSalesInDateRange(null, startOfDay, endOfDay));
        BigDecimal globalTotal = globalSalesToday.stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DailySalesUpdateWsDTO dto = DailySalesUpdateWsDTO.builder()
                .eventType(event.action())
                .warehouseUuid(event.warehouseUuid())
                .warehouseName(event.warehouseName())
                .saleUuid(event.saleUuid())
                .invoiceNumber(event.invoiceNumber())
                .operationAmount(event.amount())
                .warehouseSalesCountToday(warehouseSalesToday.size())
                .warehouseSalesTotalToday(warehouseTotal)
                .globalSalesCountToday(globalSalesToday.size())
                .globalSalesTotalToday(globalTotal)
                .timestamp(ZonedDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/warehouse/" + event.warehouseUuid() + "/daily-sales", dto);
        messagingTemplate.convertAndSend("/topic/admin/daily-sales", dto);
    }

    /**
     * Cubre la Acción 5: Total de transferencias pendientes al crear, confirmar o cancelar una transferencia.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onTransferStateChanged(TransferStateDomainEvent event) {
        long globalPending = transferRepository.count(
                TransferSpecification.hasStatusAndWarehouse(TransferStatus.PENDING, null));
        long sourcePending = transferRepository.count(
                TransferSpecification.hasStatusAndWarehouse(TransferStatus.PENDING, event.sourceWarehouseUuid()));
        long destPending = transferRepository.count(
                TransferSpecification.hasStatusAndWarehouse(TransferStatus.PENDING, event.destinationWarehouseUuid()));

        PendingTransfersUpdateWsDTO dto = PendingTransfersUpdateWsDTO.builder()
                .eventType(event.action())
                .transferUuid(event.transferUuid())
                .currentStatus(event.status())
                .sourceWarehouseUuid(event.sourceWarehouseUuid())
                .sourceWarehouseName(event.sourceWarehouseName())
                .destinationWarehouseUuid(event.destinationWarehouseUuid())
                .destinationWarehouseName(event.destinationWarehouseName())
                .totalPendingTransfersGlobal(globalPending)
                .sourceWarehousePendingTransfers(sourcePending)
                .destinationWarehousePendingTransfers(destPending)
                .timestamp(ZonedDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/admin/pending-transfers", dto);
        if (event.sourceWarehouseUuid() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/warehouse/" + event.sourceWarehouseUuid() + "/pending-transfers", dto);
        }
        if (event.destinationWarehouseUuid() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/warehouse/" + event.destinationWarehouseUuid() + "/pending-transfers", dto);
        }
    }
}