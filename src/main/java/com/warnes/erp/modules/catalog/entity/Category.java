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

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories")
//@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Category extends AuditableEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}