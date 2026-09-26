package com.warnes.erp.modules.sales.entity;

import com.warnes.erp.common.AuditableEntity;
import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.common.UuidIdentifiable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Customer extends AuditableEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "doc_number", nullable = false, unique = true, length = 20)
    private String docNumber;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "current_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id")
    private PriceList priceList;

    @Column(name = "credit_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;
}