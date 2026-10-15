package com.warnes.erp.modules.sales.service;

public interface InvoiceGeneratorService {
    String generateSaleInvoiceNumber(boolean isB2B);
    String generateCreditNoteNumber(boolean isB2B);
}