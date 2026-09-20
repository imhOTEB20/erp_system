package com.warnes.erp.modules.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record TransferCreateRequestDTO(
        @NotBlank(message = "El código de seguimiento es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String trackingCode,

        @NotNull(message = "El almacén de origen es obligatorio")
        UUID originWarehouseUuid,

        @NotNull(message = "El almacén de destino es obligatorio")
        UUID targetWarehouseUuid,

        @NotEmpty(message = "La transferencia debe contener al menos un producto")
        @Valid
        List<TransferItemRequestDTO> items
) {}