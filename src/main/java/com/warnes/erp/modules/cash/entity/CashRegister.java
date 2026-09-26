package com.warnes.erp.modules.cash.entity;

import com.warnes.erp.common.AuditableEntity;
import com.warnes.erp.common.UuidIdentifiable;
import com.warnes.erp.modules.inventory.entity.Warehouse;
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

import java.util.UUID;

@Entity
@Table(name = "cash_registers")
@Getter
@Setter
@SQLRestriction("deleted_at IS NULL")
public class CashRegister extends AuditableEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    // Relación obligatoria con la sucursal (Lazy loading para no traerla innecesariamente)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}