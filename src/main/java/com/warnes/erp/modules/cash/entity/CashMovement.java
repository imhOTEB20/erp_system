package com.warnes.erp.modules.cash.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.modules.security.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "cash_movements")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class CashMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_shift_id", nullable = false)
    private CashShift cashShift;

    // NUEVO: Trazabilidad del usuario que ejecutó o autorizó el movimiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private CashMovementType movementType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}