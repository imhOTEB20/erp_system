package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Comprobante de Nota de Crédito (Devolución/Anulación)")
public record CreditNoteResponseDTO(
        @Schema(description = "UUID interno de la NC", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uuid,
        @Schema(description = "Número legal de la NC", example = "NC-A-0001-0000012")
        String creditNoteNumber,
        @Schema(description = "UUID de la venta original", example = "330e8400-e29b-41d4-a716-446655440000")
        UUID saleUuid,
        @Schema(description = "Factura original afectada", example = "B-0001-0000123")
        String saleInvoiceNumber,
        @Schema(description = "UUID del cliente (Null si es consumidor final)", nullable = true)
        UUID customerUuid,
        @Schema(description = "Razón social del cliente", nullable = true)
        String customerName,
        @Schema(description = "Monto total devuelto", example = "75000.00")
        BigDecimal totalAmount,
        @Schema(description = "Tipo de devolución (Ej: STORE_CREDIT, CASH_REFUND)", example = "STORE_CREDIT")
        String refundType,
        @Schema(description = "Fecha de emisión UTC", example = "2026-09-24T10:00:00Z")
        ZonedDateTime createdAt,
        @Schema(description = "Lista de mercadería devuelta")
        List<CreditNoteItemResponseDTO> items
) {}