package com.warnes.erp.modules.cash.entity;

import com.warnes.erp.common.BaseEntity;
import com.warnes.erp.common.UuidIdentifiable;
import com.warnes.erp.modules.security.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cash_shifts")
@Getter
@Setter
public class CashShift extends BaseEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "opened_at", nullable = false)
    private ZonedDateTime openedAt = ZonedDateTime.now();

    @Column(name = "closed_at")
    private ZonedDateTime closedAt;

    @Column(name = "opening_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "expected_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal expectedBalance = BigDecimal.ZERO;

    @Column(name = "reported_balance", precision = 14, scale = 2)
    private BigDecimal reportedBalance;

    @Column(name = "declared_balance", precision = 14, scale = 2)
    private BigDecimal declaredBalance;

    @Column(precision = 14, scale = 2)
    private BigDecimal discrepancy;

    @Column(name = "ended_at")
    private ZonedDateTime endedAt;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShiftStatus status = ShiftStatus.OPEN;

    @OneToMany(mappedBy = "cashShift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CashMovement> movements = new ArrayList<>();
}