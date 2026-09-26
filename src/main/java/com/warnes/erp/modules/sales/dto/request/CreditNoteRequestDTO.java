package com.warnes.erp.modules.sales.dto.request;

import com.warnes.erp.modules.sales.entity.RefundType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para generar una Nota de Crédito y anular total o parcialmente una venta")
public record CreditNoteRequestDTO(
        @Schema(description = "Número legal de la Nota de Crédito", example = "NC-A-0001-0000012")
        @NotBlank(message = "El número de Nota de Crédito es obligatorio")
        @Size(max = 30)
        String creditNoteNumber,

        @Schema(description = "UUID de la factura original que se está afectando", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El UUID de la venta a afectar es obligatorio")
        UUID saleUuid,

        @Schema(description = "UUID del almacén donde reingresará físicamente la mercadería", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "El almacén de destino (donde retorna el stock) es obligatorio")
        UUID targetWarehouseUuid,

        @Schema(description = "Modalidad de la devolución (Efectivo o Saldo a Favor)", example = "STORE_CREDIT")
        @NotNull(message = "El tipo de devolución es obligatorio")
        RefundType refundType,

        @Schema(description = "Lista de productos a devolver")
        @NotEmpty(message = "Debe incluir al menos un producto a devolver")
        @Valid
        List<SaleItemRequestDTO> returnItems
) {}