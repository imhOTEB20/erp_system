package com.warnes.erp.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para la creación de un nuevo Producto/Repuesto")
public record ProductRequestDTO(
        @Schema(description = "Código SKU del fabricante. Si se omite, el sistema generará uno interno.")
        @Size(max = 50, message = "El SKU no puede exceder los 50 caracteres")
        String sku,

        @Schema(description = "Código de barras universal (EAN/UPC)")
        @Size(max = 64)
        String barcode,

        @Schema(description = "Nombre descriptivo del repuesto")
        @NotBlank(message = "El nombre del producto es obligatorio")
        String name,

        @Schema(description = "Identificador de la Categoría")
        @NotNull(message = "La categoría es obligatoria")
        UUID categoryUuid,

        @Schema(description = "Identificador de la Marca")
        @NotNull(message = "La marca es obligatoria")
        UUID brandUuid,

        @Schema(description = "Identificador del Tipo de Impuesto (Ej: IVA 21%)")
        @NotNull(message = "El tipo de impuesto es obligatorio")
        UUID taxTypeUuid,

        @Schema(description = "¿Es un servicio de taller en lugar de un bien físico?")
        Boolean service,

        @Schema(description = "¿Es un combo armado de varios repuestos?")
        Boolean kit,

        @Schema(description = "¿El producto esta disponible para venta?")
        Boolean active,

        @NotNull(message = "El costo es obligatorio")
        BigDecimal costPrice,

        @NotNull(message = "El precio final es obligatorio")
        BigDecimal salePrice,

        @Valid
        @Schema(description = "Componentes del kit (Solo aplica si kit = true)")
        List<KitItemRequestDTO> kitItems,

        List<ProductCrossReferenceRequestDTO> crossReferences
) {
        public ProductRequestDTO {
                if (service == null) service = false;
                if (kit == null) kit = false;
                if (active == null) active = true;
        }
}