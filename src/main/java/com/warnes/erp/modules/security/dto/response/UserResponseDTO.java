package com.warnes.erp.modules.security.dto.response;

import com.warnes.erp.modules.security.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Datos maestros de un usuario del sistema")
public record UserResponseDTO(
        @Schema(description = "Identificador público del usuario", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID uuid,

        @Schema(description = "Usuario de red", example = "cajero_carril")
        String username,

        @Schema(description = "Nombre real", example = "Carlos López")
        String fullName,

        @Schema(description = "Rol de acceso", example = "CASHIER")
        Role role,

        @Schema(description = "Estado de la cuenta", example = "true")
        boolean active
) {}