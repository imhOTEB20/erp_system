package com.warnes.erp.modules.catalog.entity;

import com.warnes.erp.common.AuditableEntity;
import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.common.UuidIdentifiable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tax_types")
@SQLRestriction("deleted_at IS NULL") // El filtro se queda aquí
@Getter
@Setter
public class TaxType extends AuditableEntity implements UuidIdentifiable { // <-- REGRESA LA HERENCIA

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "default_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal defaultRate;

    @Column(nullable = false)
    private boolean active = true;
}