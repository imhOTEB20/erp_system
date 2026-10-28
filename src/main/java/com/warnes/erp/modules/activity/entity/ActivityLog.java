package com.warnes.erp.modules.activity.entity;

import com.warnes.erp.common.AuditableEntity;
import com.warnes.erp.modules.cash.entity.CashRegister;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.sales.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@SQLRestriction("deleted_at IS NULL")
public class ActivityLog extends AuditableEntity {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "module", nullable = false, length = 40)
    private String module;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_uuid")
    private UUID referenceUuid;

    @Column(name = "reference_code", length = 80)
    private String referenceCode;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_warehouse_id")
    private Warehouse targetWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id")
    private CashRegister cashRegister;

    @Column(name = "performed_by", nullable = false, length = 120)
    private String performedBy;
}