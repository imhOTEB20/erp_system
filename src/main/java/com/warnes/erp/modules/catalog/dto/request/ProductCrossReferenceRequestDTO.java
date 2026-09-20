package com.warnes.erp.modules.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductCrossReferenceRequestDTO(
        @NotBlank(message = "La marca externa es obligatoria")
        @Size(max = 100)
        String externalBrand,

        @NotBlank(message = "El código (SKU) externo es obligatorio")
        @Size(max = 100)
        String externalSku
) {}