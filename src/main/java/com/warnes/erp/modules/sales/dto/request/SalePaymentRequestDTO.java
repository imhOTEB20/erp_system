package com.warnes.erp.modules.sales.dto.request;

import com.warnes.erp.modules.sales.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Payload para asentar un cobro. Soporta desglose financiero para AFIP/Posnet.")
public record SalePaymentRequestDTO(

        @Schema(description = "Medio de pago utilizado", example = "CREDIT_CARD")
        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod paymentMethod,

        @Schema(description = "Monto de capital puro que salda la deuda de la factura", example = "1000.00")
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        // Campos opcionales para la futura integraci n con la terminal
        @Schema(description = "Cantidad de cuotas elegidas", example = "3", nullable = true)
        Integer installments,

        @Schema(description = "Costo financiero puro cobrado por la financiaci n", example = "239.67", nullable = true)
        BigDecimal surchargeAmount,

        @Schema(description = "IVA calculado exclusivamente sobre el recargo financiero (Generalmente 21%)", example = "50.33", nullable = true)
        BigDecimal surchargeTaxAmount,

        @Schema(description = "Código de autorización devuelto por el Posnet", example = "004521", nullable = true)
        String authorizationCode,

        @Schema(description = "ID único de transacción del procesador (Prisma/Fiserv)", example = "TX-987654", nullable = true)
        String posnetTransactionId
) {}