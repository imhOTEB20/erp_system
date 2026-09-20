package com.warnes.erp.modules.sales.dto.request;

import com.warnes.erp.modules.sales.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CustomerPaymentRequestDTO(
        @NotNull(message = "El monto a pagar es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod paymentMethod,

        UUID cashShiftUuid // Obligatorio si el pago es en CASH
) {}