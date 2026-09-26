package com.warnes.erp.modules.purchases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para registrar el ingreso de mercadería y asentar una Factura de Compra de un Proveedor")
public record PurchaseCreateRequestDTO(
        @Schema(description = "UUID del Proveedor al que se le realiza la compra", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "El proveedor es obligatorio")
        UUID supplierUuid,

        @Schema(description = "UUID del Almacén que recepciona físicamente el stock", example = "987e6543-e21b-12d3-a456-426614174000")
        @NotNull(message = "El almacén de destino es obligatorio")
        UUID destinationWarehouseUuid,

        @Schema(description = "Número legal de la factura emitida por el proveedor", example = "FC-A-0001-0000456")
        @NotBlank(message = "El número de factura es obligatorio")
        @Size(max = 50, message = "El número de factura no puede superar los 50 caracteres")
        String invoiceNumber,

        @Schema(description = "Monto total monetario final de la factura (incluyendo impuestos)", example = "150000.00")
        @NotNull(message = "El monto total es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor a 0")
        BigDecimal totalAmount,

        @Schema(description = "Listado de repuestos que ingresan a stock físico")
        @NotEmpty(message = "La compra debe tener al menos un ítem")
        @Valid
        List<PurchaseItemRequestDTO> items,

        @Schema(description = "Listado de impuestos o percepciones a asentar en la contabilidad")
        @NotNull(message = "La lista de impuestos es obligatoria (puede ir vacía '[]' si no aplican)")
        @Valid
        List<PurchaseTaxRequestDTO> taxes
) {}