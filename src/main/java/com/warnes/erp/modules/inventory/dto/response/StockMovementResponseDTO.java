package com.warnes.erp.modules.inventory.dto.response;

import com.warnes.erp.modules.inventory.entity.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Detalle de una transacción inmutable registrada en el Kardex de inventario")
public record StockMovementResponseDTO(
        @Schema(description = "UUID del registro de movimiento", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uuid,

        @Schema(description = "SKU interno del producto movido", example = "BOSCH-BAT-75A")
        String productSku,

        @Schema(description = "Nombre del producto", example = "Batería Bosch S4 12V 75Ah")
        String productName,

        @Schema(description = "Almacén afectado", example = "Sucursal El Carril Principal")
        String warehouseName,

        @Schema(description = "Usuario que disparó la transacción", example = "jdoe")
        String userName,

        @Schema(description = "Cantidad movida (positiva o negativa)", example = "5.00")
        BigDecimal quantity,

        @Schema(description = "Saldo restante exacto del producto en ese almacén luego de la operación", example = "15.00")
        BigDecimal balanceAfter,

        @Schema(description = "Categoría de la operación", example = "SALE")
        MovementType movementType,

        @Schema(description = "UUID del documento que originó el movimiento (Venta, Compra, Transferencia)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID referenceDocument,

        @Schema(description = "Timestamp exacto de la operación", example = "2026-09-21T14:30:00Z")
        ZonedDateTime createdAt
) {}