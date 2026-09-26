package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Cabecera y detalle de una Factura de Venta")
public record SaleResponseDTO(
        @Schema(description = "UUID interno de la venta", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uuid,

        @Schema(description = "Número de comprobante fiscal", example = "B-0001-0000123")
        String invoiceNumber,

        @Schema(description = "UUID del turno de caja asociado (Null si fue operación B2B)", example = "111e4567-e89b-12d3-a456-426614174000", nullable = true)
        UUID cashShiftUuid,

        @Schema(description = "UUID del cliente comprador (Null si es consumidor final)", example = "987e6543-e21b-12d3-a456-426614174000", nullable = true)
        UUID customerUuid,

        @Schema(description = "Razón social del comprador", example = "Taller Mecánico Los Amigos SRL", nullable = true)
        String customerName,

        @Schema(description = "UUID del empleado que procesó la venta (El Fantasma)", example = "333e4567-e89b-12d3-a456-426614174000")
        UUID userUuid,

        @Schema(description = "Nombre real del empleado responsable", example = "Cristian Gerónimo")
        String userName,

        @Schema(description = "Estado contable de la factura", example = "PENDING_PAYMENT")
        String status,

        @Schema(description = "Monto final facturado", example = "150000.00")
        BigDecimal totalAmount,

        @Schema(description = "Fecha de emisión UTC", example = "2026-09-23T16:30:00Z")
        ZonedDateTime createdAt,

        @Schema(description = "Lista de mercadería procesada")
        List<SaleItemResponseDTO> items
) {}