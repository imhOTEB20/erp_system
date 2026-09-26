package com.warnes.erp.modules.purchases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Cabecera y detalle completo de una Factura de Compra procesada")
public record PurchaseResponseDTO(
        @Schema(description = "UUID interno de la compra", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uuid,

        @Schema(description = "UUID del proveedor", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID supplierUuid,

        @Schema(description = "Razón social del proveedor", example = "Fric-Rot SA")
        String supplierName,

        @Schema(description = "UUID del almacén que recibió el stock", example = "987e6543-e21b-12d3-a456-426614174000")
        UUID destinationWarehouseUuid,

        @Schema(description = "Nombre del almacén", example = "Depósito Central")
        String destinationWarehouseName,

        @Schema(description = "Número legal de la factura", example = "FC-A-0001-0000456")
        String invoiceNumber,

        @Schema(description = "Total monetario de la compra", example = "725000.00")
        BigDecimal totalAmount,

        @Schema(description = "Fecha de registro en el sistema", example = "2026-09-21T10:15:00Z")
        ZonedDateTime createdAt,

        @Schema(description = "Lista de productos ingresados")
        List<PurchaseItemResponseDTO> items,

        @Schema(description = "Lista de impuestos asentados")
        List<PurchaseTaxResponseDTO> taxes
) {}