package com.warnes.erp.modules.inventory.service;

import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.WarehouseUpdateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseAuditResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseResponseDTO;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request);
    WarehouseResponseDTO getWarehouseByUuid(UUID uuid);
    List<WarehouseResponseDTO> getAll();
    List<WarehouseResponseDTO> getAllActiveWarehouses();
    WarehouseResponseDTO updateWarehouse(UUID uuid, WarehouseUpdateRequestDTO request);
    Boolean toggleAvailability(UUID uuid);
    void deleteWarehouse(UUID uuid);
    //Metodos de auditoria y reciclaje
    List<WarehouseAuditResponseDTO> getDeletedWarehouses();
    WarehouseAuditResponseDTO restoreWarehouse(UUID uuid);
}