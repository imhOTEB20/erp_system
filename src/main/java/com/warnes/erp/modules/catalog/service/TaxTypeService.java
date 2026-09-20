package com.warnes.erp.modules.catalog.service;

import com.warnes.erp.modules.catalog.dto.request.TaxTypeRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.TaxTypeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TaxTypeService {
    TaxTypeResponseDTO createTaxType(TaxTypeRequestDTO request);
    TaxTypeResponseDTO updateTaxType(UUID uuid, TaxTypeRequestDTO request);
    TaxTypeResponseDTO getTaxTypeByUuid(UUID uuid);
    List<TaxTypeResponseDTO> getAllTaxTypes(boolean includeInactive);
    void deleteTaxType(UUID uuid);
}