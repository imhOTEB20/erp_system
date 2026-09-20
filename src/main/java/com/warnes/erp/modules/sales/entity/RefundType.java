package com.warnes.erp.modules.sales.entity;

public enum RefundType {
    CASH_REFUND,          // Sale efectivo físico de la gaveta
    STORE_CREDIT,         // Se acumula en la cuenta corriente del cliente
    BANK_TRANSFER_REFUND, // Transferencia manual (no toca la gaveta)
    CREDIT_CARD_REFUND    // Contracargo de Posnet (no toca la gaveta)
}