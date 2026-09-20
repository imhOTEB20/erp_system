package com.warnes.erp.modules.inventory.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseResponseDTO;
import com.warnes.erp.modules.inventory.service.WarehouseService;
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
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el ADMIN crea sucursales
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> create(@Valid @RequestBody WarehouseRequestDTO request) {
        WarehouseResponseDTO response = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Almacén creado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseResponseDTO>>> getAll() {
        List<WarehouseResponseDTO> response = warehouseService.getAllActiveWarehouses();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        WarehouseResponseDTO response = warehouseService.getWarehouseByUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody WarehouseRequestDTO request) {
        WarehouseResponseDTO response = warehouseService.updateWarehouse(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Almacén actualizado exitosamente"));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        warehouseService.deleteWarehouse(uuid);
        return ResponseEntity.ok(ApiResponse.success("Almacén eliminado correctamente"));
    }
}