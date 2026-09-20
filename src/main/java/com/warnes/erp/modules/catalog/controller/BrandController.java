package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.request.BrandRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
import com.warnes.erp.modules.catalog.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/brandes")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> createBrand(@Valid @RequestBody BrandRequestDTO request) {
        BrandResponseDTO response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Marca creada exitosamente"));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> updateBrand(
            @PathVariable UUID uuid,
            @Valid @RequestBody BrandRequestDTO request) {
        BrandResponseDTO response = brandService.updateBrand(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Marca actualizada"));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> getBrand(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrandByUuid(uuid)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponseDTO>>> getAllCategories(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getAllBrandes(includeInactive)));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable UUID uuid) {
        brandService.deleteBrand(uuid);
        return ResponseEntity.ok(ApiResponse.success("Marca eliminada (Soft Delete)"));
    }
}