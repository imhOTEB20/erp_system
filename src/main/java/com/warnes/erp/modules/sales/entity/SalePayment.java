package com.warnes.erp.modules.sales.entity;

import com.warnes.erp.common.BaseEntity;
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
@Table(name = "sale_payments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SalePayment extends BaseEntity implements UuidIdentifiable {

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    // Capital que impacta contra Sale.totalAmount (Saldado de deuda real)
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // --- NUEVOS CAMPOS FISCALES Y FINANCIEROS (AFIP) ---
    @Column(name = "installments")
    private Integer installments = 1;

    @Column(name = "applied_rate", precision = 6, scale = 4)
    private BigDecimal appliedRate = BigDecimal.ZERO;

    @Column(name = "surcharge_amount", precision = 14, scale = 2)
    private BigDecimal surchargeAmount = BigDecimal.ZERO;

    @Column(name = "surcharge_tax_amount", precision = 14, scale = 2)
    private BigDecimal surchargeTaxAmount = BigDecimal.ZERO; // IVA 21% sobre interes_neto

    @Column(name = "total_billed", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalBilled; // Lo que efectivamente pasa por el Posnet (amount + surcharge + tax)

    @Column(name = "authorization_code", length = 50)
    private String authorizationCode;

    @Column(name = "posnet_transaction_id", length = 100)
    private String posnetTransactionId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    public void calculateTotalBilled() {
        if (this.totalBilled == null) {
            this.totalBilled = this.amount
                    .add(this.surchargeAmount != null ? this.surchargeAmount : BigDecimal.ZERO)
                    .add(this.surchargeTaxAmount != null ? this.surchargeTaxAmount : BigDecimal.ZERO);
        }
    }
}