package com.warnes.erp.modules.security.dto.request;

import com.warnes.erp.modules.security.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede exceder los 120 caracteres")
        String fullName,

        @NotNull(message = "El rol es obligatorio")
        Role role,

        @NotNull(message = "El estado (activo/inactivo) es obligatorio")
        Boolean active
) {}