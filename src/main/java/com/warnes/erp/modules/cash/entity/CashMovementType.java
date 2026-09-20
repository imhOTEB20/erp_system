package com.warnes.erp.modules.cash.entity;

public enum CashMovementType {
    INCOME,
    EXPENSE,
    SALE_PAYMENT,
    REFUND,
    SAFE_DROP,      // Sangría: Retiro de efectivo hacia la caja fuerte
    PETTY_CASH_IN,   // Ingreso de cambio/sencillo para operar
    SHORTAGE, // Nuevo: Faltante de caja (Robo o error contra el local)
    OVERAGE   // Nuevo: Sobrante de caja (Error a favor del local)
}