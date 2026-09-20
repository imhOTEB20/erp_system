package com.warnes.erp.modules.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CustomerRequestDTO(
        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(max = 20, message = "El documento no puede superar los 20 caracteres")
        String docNumber,

        @NotBlank(message = "La razón social o nombre es obligatorio")
        @Size(max = 150, message = "La razón social no puede superar los 150 caracteres")
        String businessName,

        UUID priceListUuid // Opcional
) {}