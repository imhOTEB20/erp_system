package com.warnes.erp.modules.catalog.entity;

import com.warnes.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "kit_items")
@Getter
@Setter
public class KitItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id", nullable = false)
    private Product parentProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_product_id", nullable = false)
    private Product childProduct;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity;
}