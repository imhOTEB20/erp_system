package com.warnes.erp.modules.purchases.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.modules.catalog.entity.TaxType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_taxes")
@Getter
@Setter
public class PurchaseTax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_type_id", nullable = false)
    private TaxType taxType;

    // Monto exacto calculado del impuesto para esta factura
    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount;
}