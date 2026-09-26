package com.warnes.erp.modules.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Ficha maestra de un Cliente")
public record CustomerResponseDTO(
        @Schema(description = "UUID público del cliente", example = "987e6543-e21b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "DNI o CUIT", example = "20-35123456-9")
        String docNumber,

        @Schema(description = "Razón social", example = "Mecánica Integral SRL")
        String businessName,

        @Schema(description = "Saldo consolidado actual de la Cuenta Corriente", example = "-15000.00")
        BigDecimal currentBalance,

        @Schema(description = "UUID de la lista de precios asignada", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID priceListUuid,

        @Schema(description = "Nombre de la lista de precios asignada", example = "Taller Mayorista")
        String priceListName
) {}