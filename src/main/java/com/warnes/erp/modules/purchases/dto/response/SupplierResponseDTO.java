package com.warnes.erp.modules.purchases.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Datos maestros de un Proveedor")
public record SupplierResponseDTO(
        @Schema(description = "Identificador público del proveedor", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "Razón social o nombre", example = "Fric-Rot SA")
        String legalName,

        @Schema(description = "Identificador fiscal (CUIT/RUT)", example = "30-98765432-1")
        String taxId,

        @Schema(description = "Estado de operatividad del proveedor", example = "true")
        boolean active
) {}