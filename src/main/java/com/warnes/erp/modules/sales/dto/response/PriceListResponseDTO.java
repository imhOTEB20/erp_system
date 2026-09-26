package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Datos maestros de una Lista de Precios / Política Comercial")
public record PriceListResponseDTO(
        @Schema(description = "UUID de la lista", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "Nombre descriptivo", example = "Taller Mayorista")
        String name,

        @Schema(description = "Porcentaje de alteración del precio", example = "-15.00")
        BigDecimal marginPercentage
) {}