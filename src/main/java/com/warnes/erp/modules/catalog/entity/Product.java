package com.warnes.erp.modules.catalog.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.common.UuidIdentifiable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Product extends BaseEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(length = 64)
    private String barcode;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "is_service", nullable = false)
    private boolean service = false;

    @Column(name = "is_kit", nullable = false)
    private boolean kit = false;

    @Column(name = "cost_price", nullable = false, precision = 14, scale = 4)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "profit_margin", nullable = false, precision = 6, scale = 2)
    private BigDecimal profitMargin = BigDecimal.ZERO;

    @Column(name = "sale_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercentage;

    // Relación bidireccional para que JPA traiga las equivalencias si se las pedimos
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCrossReference> crossReferences = new ArrayList<>();

    // Relación con el armado de kits produsctos.
    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KitItem> kitItems = new ArrayList<>();
}