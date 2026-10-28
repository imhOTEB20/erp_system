package com.warnes.erp.modules.catalog.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.modules.security.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "product_price_history")
@Getter
@Setter
public class ProductPriceHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    User changedByUser;

    @Column(name = "old_cost", nullable = false, precision = 14, scale = 4)
    BigDecimal oldCost;

    @Column(name = "new_cost", nullable = false, precision = 14, scale = 4)
    BigDecimal newCost;

    @Column(name = "old_price", nullable = false, precision = 14, scale = 2)
    BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false, precision = 14, scale = 2)
    BigDecimal newPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    ZonedDateTime createdAt;
}
