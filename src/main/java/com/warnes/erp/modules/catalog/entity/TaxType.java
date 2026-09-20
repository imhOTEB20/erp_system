package com.warnes.erp.modules.catalog.entity;

import com.warnes.erp.common.UuidIdentifiable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "tax_types")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TaxType implements UuidIdentifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 50)
    private String name;

    // NUMERIC(6, 4) según esquema para manejar tasas como 21.0000 o 10.5000
    @Column(name = "default_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal defaultRate;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}