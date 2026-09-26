package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.request.CategoryRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.CategoryResponseDTO;
import com.warnes.erp.modules.catalog.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Catálogo - Categorías", description = "Clasificación y familias de productos")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Crear Categoría")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(@Valid @RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Categoría creada exitosamente"));
    }

    @Operation(summary = "Actualizar Categoría")
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable UUID uuid, @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(uuid, request), "Categoría actualizada"));
    }

    @Operation(summary = "Consultar Categoría")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategory(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryByUuid(uuid)));
    }

    @Operation(summary = "Listar todas las categorías")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(includeInactive)));
    }

    @Operation(summary = "Eliminar Categoría")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID uuid) {
        categoryService.deleteCategory(uuid);
        return ResponseEntity.ok(ApiResponse.success("Categoría eliminada (Soft Delete)"));
    }
}