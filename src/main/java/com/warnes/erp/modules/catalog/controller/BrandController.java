package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.request.BrandRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
import com.warnes.erp.modules.catalog.service.BrandService;
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
@RequestMapping("/api/v1/brandes") // Recomendación: Renombrar ruta a "/brands"
@RequiredArgsConstructor
@Tag(name = "Catálogo - Marcas", description = "Gestión de marcas fabricantes de autopartes")
public class BrandController {

    private final BrandService brandService;

    @Operation(summary = "Crear Marca")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> createBrand(@Valid @RequestBody BrandRequestDTO request) {
        BrandResponseDTO response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Marca creada exitosamente"));
    }

    @Operation(summary = "Actualizar Marca")
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> updateBrand(
            @PathVariable UUID uuid, @Valid @RequestBody BrandRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(brandService.updateBrand(uuid, request), "Marca actualizada"));
    }

    @Operation(summary = "Consultar Marca por UUID")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> getBrand(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrandByUuid(uuid)));
    }

    @Operation(summary = "Listar todas las marcas")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponseDTO>>> getAllCategories(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getAllBrandes(includeInactive)));
    }

    @Operation(summary = "Eliminar Marca", description = "Ejecuta un Soft Delete sobre el registro.")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable UUID uuid) {
        brandService.deleteBrand(uuid);
        return ResponseEntity.ok(ApiResponse.success("Marca eliminada (Soft Delete)"));
    }
}