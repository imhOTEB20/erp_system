package com.warnes.erp.modules.sales.entity;

public enum SaleStatus {
    PENDING_PAYMENT, // Nuevo estado inicial
    COMPLETED,
    CANCELLED,
    REFUND
}