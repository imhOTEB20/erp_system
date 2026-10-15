package com.warnes.erp.modules.sales.entity;

import com.warnes.erp.common.AuditableEntity;
import com.warnes.erp.common.UuidIdentifiable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_method_surcharges")
@Getter
@Setter
public class PaymentMethodSurcharge extends AuditableEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "installments")
    private Integer installments; // Nullable para planes de 1 pago o d bito

    @Column(name = "rate_percentage", nullable = false, precision = 6, scale = 4)
    private BigDecimal ratePercentage = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private ZonedDateTime effectiveFrom = ZonedDateTime.now();

    @Column(name = "effective_to")
    private ZonedDateTime effectiveTo;
}