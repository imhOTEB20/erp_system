package com.warnes.erp.modules.catalog.dto.response;

import java.util.UUID;

public record BrandResponseDTO(
        UUID uuid,
        String name,
        boolean active
) {}