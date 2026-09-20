package com.warnes.erp.modules.security.dto.response;

import com.warnes.erp.modules.security.entity.Role;
import java.util.UUID;

public record UserResponseDTO(
        UUID uuid,
        String username,
        String fullName,
        Role role,
        boolean active
) {}