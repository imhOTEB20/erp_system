package com.warnes.erp.modules.inventory.dto.response;

import java.util.UUID;

public record WarehouseResponseDTO(
        UUID uuid,
        String code,
        String name,
        boolean active
) {}