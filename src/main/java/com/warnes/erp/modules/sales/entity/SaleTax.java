package com.warnes.erp.modules.sales.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.modules.catalog.entity.TaxType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_taxes")
@Getter
@Setter
public class SaleTax extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_type_id", nullable = false)
    private TaxType taxType;

    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount;
}