package com.warnes.erp.modules.sales.dto.request;

import com.warnes.erp.modules.sales.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SalePaymentRequestDTO(
        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod paymentMethod,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount
) {}