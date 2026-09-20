package com.warnes.erp.modules.sales.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PriceListRequestDTO(
        @NotBlank(message = "El nombre de la lista de precios es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
        String name,

        @NotNull(message = "El porcentaje de margen es obligatorio")
        @DecimalMin(value = "-99.99", message = "El margen de descuento no puede exceder el -99.99%")
        BigDecimal marginPercentage
) {}