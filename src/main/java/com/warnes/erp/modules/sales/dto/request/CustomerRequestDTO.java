package com.warnes.erp.modules.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Payload para la creación o modificación de la ficha de un Cliente")
public record CustomerRequestDTO(
        @Schema(description = "Documento Nacional de Identidad o CUIT", example = "20-35123456-9")
        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(max = 20, message = "El documento no puede superar los 20 caracteres")
        String docNumber,

        @Schema(description = "Nombre completo o razón social del cliente", example = "Taller Mecánico Los Amigos SRL")
        @NotBlank(message = "La razón social o nombre es obligatorio")
        @Size(max = 150, message = "La razón social no puede superar los 150 caracteres")
        String businessName,

        @Schema(description = "UUID de la lista de precios asignada (aplica descuento/recargo automático)", example = "987e6543-e21b-12d3-a456-426614174000", nullable = true)
        UUID priceListUuid
) {}