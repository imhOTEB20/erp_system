package com.warnes.erp.modules.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión y obtener el token JWT")
public record AuthRequestDTO(
        @Schema(description = "Nombre de usuario del empleado", example = "cajero_carril")
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String username,

        @Schema(description = "Contraseña de acceso", example = "admin123")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}