package com.warnes.erp.modules.inventory.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.common.UuidIdentifiable;
import com.warnes.erp.modules.catalog.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "stocks")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Stock extends BaseEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "min_stock", nullable = false, precision = 14, scale = 2)
    private BigDecimal minStock = BigDecimal.ZERO;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;
}