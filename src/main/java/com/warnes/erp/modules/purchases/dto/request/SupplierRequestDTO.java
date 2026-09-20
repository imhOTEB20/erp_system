package com.warnes.erp.modules.purchases.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequestDTO(
        @NotBlank(message = "El nombre o razón social es obligatorio")
        @Size(max = 150, message = "El nombre legal no puede superar los 150 caracteres")
        String legalName,

        @NotBlank(message = "El identificador fiscal (CUIT/RUT) es obligatorio")
        @Size(max = 20, message = "El identificador no puede superar los 20 caracteres")
        String taxId
) {}