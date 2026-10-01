package com.warnes.erp.modules.catalog.dto.response;

import com.warnes.erp.modules.catalog.entity.KitItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Ficha maestra de producto con datos completos y equivalencias")
public record ProductResponseDTO(
        @Schema(description = "Identificador público del producto", example = "987e6543-e21b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "SKU Interno", example = "BOSCH-BAT-75A")
        String sku,

        @Schema(description = "Código de barras", example = "7791234567890")
        String barcode,

        @Schema(description = "Nombre del producto", example = "Batería Bosch S4 12V 75Ah")
        String name,

        @Schema(description = "Categoría a la que pertenece")
        CategoryResponseDTO category,

        @Schema(description = "Marca fabricante")
        BrandResponseDTO brand,

        @Schema(description = "Indica si es servicio intangible", example = "false")
        boolean service,

        @Schema(description = "Indica si es un kit de componentes", example = "false")
        boolean kit,

        @Schema(description = "Indica si el producto esta activo o inhactivo para venta.")
        boolean active,

        @Schema(description = "Precio de costo actual", example = "45000.00")
        BigDecimal costPrice,

        @Schema(description = "Margen rentabilidad", example = "30.00")
        BigDecimal profitMargin,

        @Schema(description = "Precio de venta actual calculado", example = "70785.00")
        BigDecimal salePrice,

        @Schema(description = "Impuesto asociado")
        TaxTypeResponseDTO taxType,

        @Schema(description = "Códigos equivalentes de la competencia")
        List<ProductCrossReferenceResponseDTO> crossReferences,

        @Schema(description = "Lista de productos que pertenecen al kit, si es un kit.")
        List<KitItem> kitItems
) {}