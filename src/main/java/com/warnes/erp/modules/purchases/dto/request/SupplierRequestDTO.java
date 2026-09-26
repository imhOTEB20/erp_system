package com.warnes.erp.modules.purchases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para la creación o modificación de un Proveedor")
public record SupplierRequestDTO(
        @Schema(description = "Razón social o nombre legal del proveedor", example = "Fric-Rot SA")
        @NotBlank(message = "El nombre o razón social es obligatorio")
        @Size(max = 150, message = "El nombre legal no puede superar los 150 caracteres")
        String legalName,

        @Schema(description = "Identificador fiscal (CUIT/RUT/NIT)", example = "30-98765432-1")
        @NotBlank(message = "El identificador fiscal (CUIT/RUT) es obligatorio")
        @Size(max = 20, message = "El identificador no puede superar los 20 caracteres")
        String taxId
) {}