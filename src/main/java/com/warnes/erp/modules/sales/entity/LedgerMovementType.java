package com.warnes.erp.modules.sales.entity;

public enum LedgerMovementType {
    STORE_CREDIT_ISSUED,   // Ingreso: Saldo a favor generado (Ej: Nota de Crédito)
    PAYMENT_WITH_CREDIT,   // Egreso: Compra pagada con saldo a favor
    MANUAL_ADJUSTMENT,      // Ajuste manual por el administrador
    SALE_ON_CREDIT,  // Egreso: La deuda del cliente aumenta por una compra
    DEBT_PAYMENT     // Ingreso: El cliente vino el viernes a pagar su deuda
}