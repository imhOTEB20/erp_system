package com.warnes.erp.modules.sales.dto.request;

import com.warnes.erp.modules.sales.entity.RefundType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreditNoteRequestDTO(
        @NotBlank(message = "El número de Nota de Crédito es obligatorio")
        @Size(max = 30)
        String creditNoteNumber,

        @NotNull(message = "El UUID de la venta a afectar es obligatorio")
        UUID saleUuid,

        @NotNull(message = "El almacén de destino (donde retorna el stock) es obligatorio")
        UUID targetWarehouseUuid,

        @NotNull(message = "El tipo de devolución es obligatorio")
        RefundType refundType,

        @NotEmpty(message = "Debe incluir al menos un producto a devolver")
        @Valid
        List<SaleItemRequestDTO> returnItems // Reutilizamos el DTO de ítems (UUID producto + cantidad)
) {}