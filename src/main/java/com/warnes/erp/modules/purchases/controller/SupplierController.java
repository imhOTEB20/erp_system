package com.warnes.erp.modules.purchases.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.purchases.dto.request.SupplierRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.SupplierResponseDTO;
import com.warnes.erp.modules.purchases.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores crean proveedores
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> create(@Valid @RequestBody SupplierRequestDTO request) {
        SupplierResponseDTO response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Proveedor creado exitosamente"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')") // Lectura compartida
    public ResponseEntity<ApiResponse<List<SupplierResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAllActiveSuppliers()));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplierByUuid(uuid)));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody SupplierRequestDTO request) {
        SupplierResponseDTO response = supplierService.updateSupplier(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Proveedor actualizado exitosamente"));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        supplierService.deleteSupplier(uuid);
        return ResponseEntity.ok(ApiResponse.success("Proveedor eliminado correctamente"));
    }
}