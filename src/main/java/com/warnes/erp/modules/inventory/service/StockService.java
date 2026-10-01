package com.warnes.erp.modules.inventory.service;

import com.warnes.erp.modules.catalog.dto.response.ProductSimpleResponseDTO;
import com.warnes.erp.modules.inventory.dto.request.InitialStockRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.StockAdjustmentRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.ProductStockResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;

import java.util.List;
import java.util.UUID;

public interface StockService {
    void adjustStock(StockAdjustmentRequestDTO request);
    ProductStockResponseDTO initializerStock(InitialStockRequestDTO request);
    List<StockMovementResponseDTO> getProductKardex(UUID productUuid);
    List<ProductStockResponseDTO> getProductsByWarehouse(UUID warehouseUuid);
    List<ProductSimpleResponseDTO> getProductNonStockInWarehouse(UUID warehouseUuid);
}