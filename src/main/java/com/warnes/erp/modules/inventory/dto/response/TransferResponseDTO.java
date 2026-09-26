package com.warnes.erp.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Cabecera y estado de una orden de transferencia entre almacenes")
public record TransferResponseDTO(
        @Schema(description = "UUID interno de la transferencia", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uuid,

        @Schema(description = "Código de seguimiento", example = "TRF-2026-0001")
        String trackingCode,

        @Schema(description = "UUID almacén origen", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID originWarehouseUuid,

        @Schema(description = "Nombre almacén origen", example = "Depósito Central")
        String originWarehouseName,

        @Schema(description = "UUID almacén destino", example = "987e6543-e21b-12d3-a456-426614174000")
        UUID targetWarehouseUuid,

        @Schema(description = "Nombre almacén destino", example = "Sucursal El Carril Principal")
        String targetWarehouseName,

        @Schema(description = "Estado logístico", example = "PENDING")
        String status,

        @Schema(description = "Fecha de creación del remito", example = "2026-09-21T09:00:00Z")
        ZonedDateTime createdAt,

        @Schema(description = "Lista de mercadería despachada")
        List<TransferItemResponseDTO> items
) {}