package com.warnes.erp.modules.inventory.service;

import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseResponseDTO;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request);
    WarehouseResponseDTO getWarehouseByUuid(UUID uuid);
    List<WarehouseResponseDTO> getAllActiveWarehouses();
    WarehouseResponseDTO updateWarehouse(UUID uuid, WarehouseRequestDTO request);
    void deleteWarehouse(UUID uuid);
}