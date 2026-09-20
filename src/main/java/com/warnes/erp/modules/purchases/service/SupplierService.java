package com.warnes.erp.modules.purchases.service;

import com.warnes.erp.modules.purchases.dto.request.SupplierRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.SupplierResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SupplierService {
    SupplierResponseDTO createSupplier(SupplierRequestDTO request);
    SupplierResponseDTO getSupplierByUuid(UUID uuid);
    List<SupplierResponseDTO> getAllActiveSuppliers();
    SupplierResponseDTO updateSupplier(UUID uuid, SupplierRequestDTO request);
    void deleteSupplier(UUID uuid);
}