package com.warnes.erp.modules.purchases.dto.response;
import java.util.UUID;

public record SupplierResponseDTO(
        UUID uuid,
        String legalName,
        String taxId,
        boolean active
) {}