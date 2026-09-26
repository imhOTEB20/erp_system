package com.warnes.erp.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Payload para la definición de tasas y alícuotas impositivas")
public record TaxTypeRequestDTO(
        @Schema(description = "Nombre oficial del impuesto", example = "IVA Responsable Inscripto")
        @NotBlank(message = "El nombre del impuesto es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
        String name,

        @Schema(description = "Tasa nominal por defecto del impuesto", example = "21.0000")
        @NotNull(message = "La tasa por defecto es obligatoria")
        @DecimalMin(value = "0.0000", message = "La tasa no puede ser negativa")
        @DecimalMax(value = "99.9999", message = "La tasa no puede exceder el límite permitido")
        BigDecimal defaultRate,

        @Schema(description = "Estado de aplicación del impuesto", example = "true")
        @NotNull(message = "El estado (activo/inactivo) es obligatorio")
        Boolean active
) {}