package com.warnes.erp.modules.catalog.entity;

import com.warnes.erp.common.AuditableEntity;
import com.warnes.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime; // <-- IMPORTANTE

@Entity
@Table(name = "product_cross_references")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class ProductCrossReference extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "external_brand", nullable = false, length = 100)
    private String externalBrand;

    @Column(name = "external_sku", nullable = false, length = 100)
    private String externalSku;

}