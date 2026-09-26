package com.warnes.erp.modules.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Payload para la creación o modificación de una Lista de Precios")
public record PriceListRequestDTO(
        @Schema(description = "Nombre identificador de la lista", example = "Taller Mayorista")
        @NotBlank(message = "El nombre de la lista de precios es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
        String name,

        @Schema(description = "Porcentaje de margen aplicado sobre el precio base. Usar valores negativos para descuentos (Ej: -15.00 para 15% de descuento).", example = "-15.00")
        @NotNull(message = "El porcentaje de margen es obligatorio")
        @DecimalMin(value = "-99.99", message = "El margen de descuento no puede exceder el -99.99%")
        BigDecimal marginPercentage
) {}