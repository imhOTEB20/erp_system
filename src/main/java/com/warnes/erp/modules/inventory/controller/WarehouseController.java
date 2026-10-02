package com.warnes.erp.modules.inventory.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.service.CashRegisterService;
import com.warnes.erp.modules.catalog.dto.response.ProductSimpleResponseDTO;
import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.WarehouseUpdateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.ProductStockResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseAuditResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseResponseDTO;
import com.warnes.erp.modules.inventory.service.StockService;
import com.warnes.erp.modules.inventory.service.WarehouseService;
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
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Inventario - Almacenes", description = "Configuración de sucursales, depósitos y puntos físicos de venta")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final CashRegisterService cashRegisterService;
    private final StockService stockService;

    @Operation(summary = "Crear Almacén", description = "Registra una nueva sucursal física en el sistema.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> create(@Valid @RequestBody WarehouseRequestDTO request) {
        WarehouseResponseDTO response = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Almacén creado exitosamente"));
    }

    @Operation(summary = "Listar todos los almacenes, salvo los eliminados")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseResponseDTO>>> getAll() {
        List<WarehouseResponseDTO> response = warehouseService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Listar Almacenes Activos")
    @GetMapping("/actives")
    public ResponseEntity<ApiResponse<List<WarehouseResponseDTO>>> getAllActives() {
        List<WarehouseResponseDTO> response = warehouseService.getAllActiveWarehouses();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Consultar Almacén")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        WarehouseResponseDTO response = warehouseService.getWarehouseByUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Alterna disponibilidad de una sucursal")
    @PatchMapping("/{uuid}/toggle_availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID uuid) {
        Boolean newState = warehouseService.toggleAvailability(uuid);
        return ResponseEntity.ok(ApiResponse.success(String.format("Sucursal %s con exito", newState ? "activada" : "desactivada")));
    }

    @Operation(summary = "Actualizar Almacén (Parcial o Total)")
    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody WarehouseUpdateRequestDTO request) {
        WarehouseResponseDTO response = warehouseService.updateWarehouse(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Almacén actualizado exitosamente"));
    }

    @Operation(summary = "Eliminar Almacén", description = "Inhabilita operativamente un almacén (Soft Delete).")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        warehouseService.deleteWarehouse(uuid);
        return ResponseEntity.ok(ApiResponse.success("Almacén eliminado correctamente"));
    }

    //endpoint de reciclaje y administrativos.
    @Operation(summary = "Listar Almacenes Eliminados", description = "Papelera de reciclaje: muestra almacenes dados de baja.")
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<WarehouseAuditResponseDTO>>> getDeleted() {
        List<WarehouseAuditResponseDTO> response = warehouseService.getDeletedWarehouses();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Restaurar Almacén", description = "Recupera un almacén previamente eliminado (Soft Delete).")
    @PatchMapping("/{uuid}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseAuditResponseDTO>> restore(@PathVariable UUID uuid) {
        WarehouseAuditResponseDTO response = warehouseService.restoreWarehouse(uuid);
        return ResponseEntity.ok(ApiResponse.success(response, "Almacén restaurado exitosamente"));
    }

    //endpoins refentes a las cajas registradoras de una sucursal
    @Operation(summary = "Listar Cajas por Sucursal", description = "Devuelve todas las cajas registradoras habilitadas dentro de un almacén específico.")
    @GetMapping("/{uuid}/cash-registers")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<CashRegisterResponseDTO>>> getRegistersByWarehouse(@PathVariable UUID uuid) {
        List<CashRegisterResponseDTO> response = cashRegisterService.getRegistersByWarehouse(uuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //endpoins refentes a los productos disponibles de una sucursal
    @Operation(summary = "Listar stock por producto de una sucursal", description = "Devuelve todos los productos disponibles dentro de un almacén específico.")
    @GetMapping("/{uuid}/stocks")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<ProductStockResponseDTO>>> getProductByWarehouse(@PathVariable UUID uuid) {

        List<ProductStockResponseDTO> response = stockService.getProductsByWarehouse(uuid);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Inventario recuperado exitosamente.")
        );
    }

    @Operation(summary = "Listar productos que no tienen stock en una sucursal.", description = "Devuelve todos los productos que no tienen stock en una sucursal determinada.")
    @GetMapping("/{uuid}/non_stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<ProductSimpleResponseDTO>>> getProductNonStockInWarehouse(@PathVariable UUID uuid) {
        List<ProductSimpleResponseDTO> response = stockService.getProductNonStockInWarehouse(uuid);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Productos con stock faltantes recuperado exitosamente.")
        );
    }
}