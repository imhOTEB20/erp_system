package com.warnes.erp.modules.sales.dto.request;

import com.warnes.erp.modules.sales.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload para registrar un abono o pago a una Cuenta Corriente (Deuda) de un cliente")
public record CustomerPaymentRequestDTO(
        @Schema(description = "Monto monetario a abonar", example = "25000.00")
        @NotNull(message = "El monto a pagar es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @Schema(description = "Medio con el cual el cliente entrega el dinero", example = "CASH")
        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod paymentMethod,

        @Schema(description = "UUID del turno de caja activo (Requerido solo si el pago es en efectivo)", example = "123e4567-e89b-12d3-a456-426614174000", nullable = true)
        UUID cashShiftUuid
) {}