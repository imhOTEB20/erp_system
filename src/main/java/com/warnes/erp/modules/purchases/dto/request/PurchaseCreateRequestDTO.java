package com.warnes.erp.modules.purchases.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PurchaseCreateRequestDTO(
        @NotNull(message = "El proveedor es obligatorio")
        UUID supplierUuid,

        @NotNull(message = "El almacén de destino es obligatorio")
        UUID destinationWarehouseUuid,

        @NotBlank(message = "El número de factura es obligatorio")
        @Size(max = 50, message = "El número de factura no puede superar los 50 caracteres")
        String invoiceNumber,

        @NotNull(message = "El monto total es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor a 0")
        BigDecimal totalAmount,

        @NotEmpty(message = "La compra debe tener al menos un ítem")
        @Valid
        List<PurchaseItemRequestDTO> items,

        @NotNull(message = "La lista de impuestos es obligatoria (puede ir vacía '[]' si no aplican)")
        @Valid
        List<PurchaseTaxRequestDTO> taxes
) {}