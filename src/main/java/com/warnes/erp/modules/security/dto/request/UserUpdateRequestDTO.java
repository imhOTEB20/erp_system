package com.warnes.erp.modules.security.dto.request;

import com.warnes.erp.modules.security.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para modificar los datos o el rol de un usuario existente")
public record UserUpdateRequestDTO(
        @Schema(description = "Nombre real del empleado", example = "Carlos López")
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede exceder los 120 caracteres")
        String fullName,

        @Schema(description = "Rol de autorización en el sistema", example = "ADMIN")
        @NotNull(message = "El rol es obligatorio")
        Role role,

        @Schema(description = "Estado de acceso al sistema (Inhabilitar para bloquear acceso)", example = "true")
        @NotNull(message = "El estado (activo/inactivo) es obligatorio")
        Boolean active
) {}