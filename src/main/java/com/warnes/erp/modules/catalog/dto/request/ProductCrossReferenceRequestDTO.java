package com.warnes.erp.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para registrar un código equivalente (cross-reference) de otra marca para un producto")
public record ProductCrossReferenceRequestDTO(
        @Schema(description = "Nombre comercial de la marca de competencia o fabricante", example = "Fram")
        @NotBlank(message = "La marca externa es obligatoria")
        @Size(max = 100)
        String externalBrand,

        @Schema(description = "Código SKU bajo el cual la marca externa comercializa la pieza", example = "PH2870A")
        @NotBlank(message = "El código (SKU) externo es obligatorio")
        @Size(max = 100)
        String externalSku
) {}