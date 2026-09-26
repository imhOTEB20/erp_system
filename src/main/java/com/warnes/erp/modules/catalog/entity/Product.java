package com.warnes.erp.modules.catalog.entity;

import com.warnes.erp.common.AuditableEntity;
import com.warnes.erp.common.UuidIdentifiable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Product extends AuditableEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 50)
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

    // NUEVO: Relación maestra con el impuesto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_type_id", nullable = false)
    private TaxType taxType;

    @Column(name = "is_service", nullable = false)
    private boolean service = false;

    @Column(name = "is_kit", nullable = false)
    private boolean kit = false;

    @Column(name = "cost_price", nullable = false, precision = 14, scale = 4)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "sale_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal salePrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCrossReference> crossReferences = new ArrayList<>();

    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KitItem> kitItems = new ArrayList<>();
}