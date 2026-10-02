package com.warnes.erp.modules.cash.entity;

public enum CashMovementType {
    INCOME,            // Ingreso manual
    EXPENSE,           // Egreso manual (pago a proveedores menores, etc)
    SALE_PAYMENT,      // Ingreso por venta
    REFUND,            // Egreso por nota de crédito / devolución
    SAFE_DROP,         // Sangría (retiro a caja fuerte)
    PETTY_CASH_IN,     // Ingreso de sencillo
    SHORTAGE,          // Faltante de caja detectado en el cierre ciego
    OVERAGE,           // Sobrante de caja detectado en el cierre ciego
    SYSTEM_ADJUSTMENT  // NUEVO: Ajuste del sistema (Ej: Reapertura de turno por un ADMIN)
}