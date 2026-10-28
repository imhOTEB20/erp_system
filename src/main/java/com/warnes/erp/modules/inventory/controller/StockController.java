package com.warnes.erp.modules.inventory.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.inventory.dto.request.InitialStockRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.StockAdjustmentRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.ProductStockAlertResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.ProductStockResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;
import com.warnes.erp.modules.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Inventario - Stock y Kardex", description = "Ajustes manuales de mercadería y auditoría inmutable de movimientos")
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Ajuste manual de stock", description = "Suma o resta stock físico por rotura, robo o recuento. Deja registro inmutable en el Kardex.")
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> adjustStock(@Valid @RequestBody StockAdjustmentRequestDTO request) {
        stockService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock actualizado correctamente"));
    }

    @Operation(summary = "Inicializar stock en tienda", description = "Inicializa el stock de un producto en una determinada tienda. Deja registro inmutable en el Kardex.")
    @PostMapping("/initializer")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductStockResponseDTO>> initializerStock(@Valid @RequestBody InitialStockRequestDTO request) {
        ProductStockResponseDTO response = stockService.initializerStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock inicializado exitosamente"));
    }

    @Operation(summary = "Consultar Kardex de Producto", description = "Devuelve el historial completo de entradas y salidas de un producto.")
    @GetMapping("/kardex/{productUuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<List<StockMovementResponseDTO>>> getKardex(@PathVariable UUID productUuid) {
        List<StockMovementResponseDTO> response = stockService.getProductKardex(productUuid);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Consultar Producto con stock minimo en una sucursal", description = "Devuelve los identificadores de productos y warehouse, donde el product tenga stock por debajo del minimo.")
    @GetMapping("/below-min/{warehouseUuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProductStockAlertResponseDTO>>> getBelowMin(@PathVariable UUID warehouseUuid) {
        List<ProductStockAlertResponseDTO> response = stockService.getProductsBelowMinStockDetailed(warehouseUuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}