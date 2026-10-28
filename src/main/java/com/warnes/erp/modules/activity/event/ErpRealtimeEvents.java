package com.warnes.erp.modules.activity.event;

import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.inventory.entity.OperationSource;
import com.warnes.erp.modules.inventory.entity.TransferStatus;

import java.math.BigDecimal;
import java.util.UUID;

public final class ErpRealtimeEvents {

    private ErpRealtimeEvents() {}

    /**
     * Se dispara cada vez que cambia el stock de un producto (venta, transferencia, compra, ajuste, devolución).
     */
    public record StockChangedDomainEvent(
            UUID warehouseUuid,
            String warehouseName,
            UUID productUuid,
            String productSku,
            String productName,
            BigDecimal previousQuantity,
            BigDecimal newQuantity,
            BigDecimal minStock,
            OperationSource operationSource
    ) {}

    /**
     * Se dispara al abrir o cerrar un turno de caja en una sucursal.
     */
    public record CashShiftDomainEvent(
            UUID shiftUuid,
            ShiftStatus status,
            UUID cashRegisterUuid,
            String cashRegisterName,
            UUID warehouseUuid,
            String warehouseName,
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            String performedBy
    ) {}

    /**
     * Se dispara cuando se cobra una venta o se cancela/devuelve una venta que ya fue cobrada.
     */
    public record DailySalesDomainEvent(
            String action, // "SALE_COMPLETED" o "SALE_CANCELLED_OR_REFUNDED"
            UUID warehouseUuid,
            String warehouseName,
            UUID saleUuid,
            String invoiceNumber,
            BigDecimal amount
    ) {}

    /**
     * Se dispara cuando se crea, confirma o cancela una transferencia.
     */
    public record TransferStateDomainEvent(
            String action, // "TRANSFER_CREATED", "TRANSFER_CONFIRMED", "TRANSFER_CANCELLED"
            UUID transferUuid,
            TransferStatus status,
            UUID sourceWarehouseUuid,
            String sourceWarehouseName,
            UUID destinationWarehouseUuid,
            String destinationWarehouseName
    ) {}
}