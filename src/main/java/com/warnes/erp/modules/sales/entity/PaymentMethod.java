package com.warnes.erp.modules.sales.entity;

public enum PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    TRANSFER,
    WALLET,
    STORE_CREDIT,        // Pago con saldo a favor del cliente
    ROUNDING_ADJUSTMENT,  // Ajuste contable para cuadrar centavos
    ON_CREDIT  // Nuevo: El cliente se lleva la mercadería y asume deuda
}