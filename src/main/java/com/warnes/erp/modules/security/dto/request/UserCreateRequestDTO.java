package com.warnes.erp.modules.security.dto.request;

import com.warnes.erp.modules.security.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para registrar un nuevo usuario (empleado) en el sistema")
public record UserCreateRequestDTO(
        @Schema(description = "Nombre de usuario para login", example = "cajero_carril")
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres")
        String username,

        @Schema(description = "Contraseña de acceso plana (se encriptará automáticamente)", example = "Admin123!")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @Schema(description = "Nombre real del empleado para auditoría", example = "Carlos López")
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede exceder los 120 caracteres")
        String fullName,

        @Schema(description = "Rol de autorización en el sistema", example = "CASHIER")
        @NotNull(message = "El rol es obligatorio")
        Role role
) {}