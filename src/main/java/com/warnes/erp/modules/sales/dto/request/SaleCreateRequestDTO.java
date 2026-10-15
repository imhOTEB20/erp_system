package com.warnes.erp.modules.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para iniciar una nueva venta en mostrador (POS) o backoffice (B2B)")
public record SaleCreateRequestDTO(

        @Schema(description = "UUID del almacén/sucursal origen (Ej: Sucursal El Carril)", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "El almacén de origen es obligatorio")
        UUID originWarehouseUuid,

        @Schema(description = "UUID del cliente. Enviar null si es Consumidor Final de mostrador", example = "987e6543-e21b-12d3-a456-426614174000", nullable = true)
        UUID customerUuid,

        @Schema(description = "Flag que indica si es una venta mayorista B2B (No exige turno de caja)", example = "false")
        boolean isB2B,

        @Schema(description = "Listado de repuestos a facturar")
        @NotEmpty(message = "La venta debe contener al menos un ítem")
        @Valid
        List<SaleItemRequestDTO> items
) {}