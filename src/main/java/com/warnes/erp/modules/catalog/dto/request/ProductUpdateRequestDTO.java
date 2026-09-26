package com.warnes.erp.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload para la actualización parcial de un Producto (solo se envían los campos a modificar)")
public record ProductUpdateRequestDTO(
        @Schema(description = "Nuevo nombre descriptivo")
        @Size(max = 200, message = "El nombre no puede exceder los 200 caracteres")
        String name,

        @Schema(description = "Nuevo identificador de Categoría")
        UUID categoryUuid,

        @Schema(description = "Nuevo identificador de Marca")
        UUID brandUuid,

        @Schema(description = "Nuevo identificador de Impuesto")
        UUID taxTypeUuid,

        @Schema(description = "Cambiar indicador de servicio")
        Boolean service,

        @Schema(description = "Cambiar indicador de kit")
        Boolean kit,

        @Schema(description = "Actualizar precio de costo")
        BigDecimal costPrice,

        @Schema(description = "Actualizar precio de venta")
        BigDecimal salePrice
) {}