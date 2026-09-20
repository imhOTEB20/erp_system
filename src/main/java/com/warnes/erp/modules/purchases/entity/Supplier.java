package com.warnes.erp.modules.purchases.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.common.UuidIdentifiable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Supplier extends BaseEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "legal_name", nullable = false, length = 150)
    private String legalName;

    @Column(name = "tax_id", nullable = false, unique = true, length = 20)
    private String taxId;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;
}