package com.warnes.erp.modules.inventory.service;

import com.warnes.erp.modules.inventory.dto.request.StockAdjustmentRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;

import java.util.List;
import java.util.UUID;

public interface StockService {
    void adjustStock(StockAdjustmentRequestDTO request);
    List<StockMovementResponseDTO> getProductKardex(UUID productUuid);
}