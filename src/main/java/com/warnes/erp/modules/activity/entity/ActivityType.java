package com.warnes.erp.modules.activity.entity;

public enum ActivityType {
    // Ventas y Facturación
    SALE_CREATED,
    SALE_COMPLETED,
    SALE_CANCELLED,
    CREDIT_NOTE_ISSUED,
    CUSTOMER_PAYMENT_REGISTERED,

    // Inventario y Transferencias
    TRANSFER_CREATED,
    TRANSFER_DISPATCHED,
    TRANSFER_RECEIVED,
    TRANSFER_CANCELLED,
    STOCK_INITIALIZED,
    STOCK_ADJUSTED,

    // Compras
    PURCHASE_REGISTERED,
    SUPPLIER_CREATED,

    // Caja y Turnos
    CASH_SHIFT_OPENED,
    CASH_SHIFT_CLOSED,
    CASH_MOVEMENT_REGISTERED,
    CASH_ADJUSTMENT_REGISTERED,

    // Catálogo y Precios
    PRODUCT_CREATED,
    PRICE_LIST_UPDATED
}