package com.warnes.erp.modules.inventory.dto.response;

import java.util.UUID;

public record TransferItemResponseDTO(
        UUID productUuid,
        String productSku,
        String productName,
        int quantity
) {}