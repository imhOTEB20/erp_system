package com.warnes.erp.modules.sales.entity;

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
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_lists")
@SQLRestriction("deleted_at IS NULL") // NUEVO: Filtro automático de Hibernate
@Getter
@Setter
public class PriceList extends AuditableEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "margin_percentage", nullable = false, precision = 6, scale = 2)
    private BigDecimal marginPercentage = BigDecimal.ZERO;

}