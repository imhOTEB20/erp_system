package com.warnes.erp.modules.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para Venta de Mostrador (One-Step Checkout). Combina los items y los cobros en una sola transacción ACID.")
public record SaleCheckoutRequestDTO(

        @Schema(description = "UUID del cliente. Enviar null si es Consumidor Final", nullable = true)
        UUID customerUuid,

        @Schema(description = "Listado de repuestos a facturar")
        @NotEmpty(message = "La venta debe contener al menos un ítem")
        @Valid
        List<SaleItemRequestDTO> items,

        @Schema(description = "Listado de pagos (Soporta múltiples métodos, ej: Efectivo + Tarjeta)")
        @NotEmpty(message = "Debe proporcionar al menos un método de pago")
        @Valid
        List<SalePaymentRequestDTO> payments
) {}