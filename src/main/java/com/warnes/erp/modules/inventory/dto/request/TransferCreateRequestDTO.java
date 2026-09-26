package com.warnes.erp.modules.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para iniciar una transferencia de mercadería entre sucursales")
public record TransferCreateRequestDTO(

        @Schema(description = "UUID del almacén desde donde sale la mercadería", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "El almacén de origen es obligatorio")
        UUID originWarehouseUuid,

        @Schema(description = "UUID del almacén destino que recibirá la mercadería", example = "987e6543-e21b-12d3-a456-426614174000")
        @NotNull(message = "El almacén de destino es obligatorio")
        UUID targetWarehouseUuid,

        @Schema(description = "Lista de productos a transferir")
        @NotEmpty(message = "La transferencia debe contener al menos un producto")
        @Valid
        List<TransferItemRequestDTO> items
) {}