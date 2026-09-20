package com.warnes.erp.modules.security.dto.response;

import java.util.UUID;

public record AuthResponseDTO(
        String token,
        UUID userUuid,
        String fullName,
        String role
) {}