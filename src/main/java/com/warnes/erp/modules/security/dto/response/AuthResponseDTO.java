package com.warnes.erp.modules.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Respuesta exitosa de autenticación con el Token de acceso")
public record AuthResponseDTO(
        @Schema(description = "Token JWT firmado para incluir en las cabeceras Bearer", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "UUID del empleado autenticado", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userUuid,

        @Schema(description = "Nombre completo del empleado", example = "Carlos López")
        String fullName,

        @Schema(description = "Rol otorgado", example = "CASHIER")
        String role,

        @Schema(description = "Uuid de la sucursal default del usuario.")
        UUID defaultWarehouseUuid, // NUEVO

        @Schema(description = "Nombre de la sucursal default del usuario.")
        String defaultWarehouseName // NUEVO
) {}