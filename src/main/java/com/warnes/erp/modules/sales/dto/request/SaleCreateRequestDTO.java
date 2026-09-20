package com.warnes.erp.modules.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record SaleCreateRequestDTO(
        @NotBlank(message = "El número de factura es obligatorio")
        @Size(max = 30, message = "El número de factura no puede superar los 30 caracteres")
        String invoiceNumber,

        @NotNull(message = "El almacén de origen (para deducir stock) es obligatorio")
        UUID originWarehouseUuid,

        UUID customerUuid, // Opcional: Si es nulo, es venta al público (Consumidor Final)

        @NotEmpty(message = "La venta debe contener al menos un ítem")
        @Valid
        List<SaleItemRequestDTO> items
) {}