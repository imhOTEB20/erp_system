package com.warnes.erp.modules.sales.service.impl;

import com.warnes.erp.modules.sales.service.InvoiceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceGeneratorServiceImpl implements InvoiceGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    // El Punto de Venta (POS) declarado en AFIP para esta sucursal física.
    // Podría venir de la tabla Warehouse en un futuro, por ahora lo fijamos en 0001.
    private static final String POINT_OF_SALE = "0001";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateSaleInvoiceNumber(boolean isB2B) {
        String sequenceName = isB2B ? "seq_invoice_type_a" : "seq_invoice_type_b";
        String letter = isB2B ? "A" : "B";

        // Llamada nativa y atómica a PostgreSQL
        Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('" + sequenceName + "')", Long.class);

        // Formato AFIP: Letra-PuntoVenta(4)-Correlativo(8) -> A-0001-00000123
        return String.format("%s-%s-%08d", letter, POINT_OF_SALE, nextVal);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateCreditNoteNumber(boolean isB2B) {
        String sequenceName = isB2B ? "seq_credit_note_type_a" : "seq_credit_note_type_b";
        String letter = isB2B ? "NCA" : "NCB"; // Nota de Crédito A o B

        Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('" + sequenceName + "')", Long.class);
        return String.format("%s-%s-%08d", letter, POINT_OF_SALE, nextVal);
    }
}