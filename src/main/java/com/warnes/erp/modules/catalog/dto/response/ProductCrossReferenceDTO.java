package com.warnes.erp.modules.catalog.dto.response;

import java.util.UUID;

public record ProductCrossReferenceDTO(
        UUID uuid,
        String externalBrand,
        String externalSku
) {}