package com.warnes.erp.modules.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request para concretar una venta pendiente.")
public record PayRequestDTO(
        @Schema(description = "UUID de la sucursar donde se concreta la venta.", nullable = true)
        UUID warehouseUuid,

        @Schema(description = "Listado de pagos (Soporta múltiples métodos, ej: Efectivo + Tarjeta)")
        @NotEmpty(message = "Debe proporcionar al menos un método de pago")
        @Valid
        List<SalePaymentRequestDTO> payments
) {
}
