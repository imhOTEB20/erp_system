package com.warnes.erp.modules.inventory.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.inventory.dto.request.StockAdjustmentRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;
import com.warnes.erp.modules.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')") // Restricción estricta
    public ResponseEntity<ApiResponse<Void>> adjustStock(@Valid @RequestBody StockAdjustmentRequestDTO request) {
        stockService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock actualizado correctamente"));
    }

    @GetMapping("/kardex/{productUuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<List<StockMovementResponseDTO>>> getKardex(@PathVariable UUID productUuid) {
        List<StockMovementResponseDTO> response = stockService.getProductKardex(productUuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}