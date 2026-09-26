package com.warnes.erp.modules.sales.dto.request;

import com.warnes.erp.modules.sales.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Payload para asentar un pago parcial o total a una venta pendiente")
public record SalePaymentRequestDTO(
        @Schema(description = "Modalidad de pago utilizada por el cliente", example = "CREDIT_CARD")
        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod paymentMethod,

        @Schema(description = "Monto monetario entregado", example = "150000.00")
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount
) {}