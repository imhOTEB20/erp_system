package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BulkImportResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> create(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Producto creado exitosamente."));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        ProductResponseDTO response = productService.getProductByUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO response = productService.updateProduct(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Producto actualizado exitosamente"));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        productService.deleteProduct(uuid);
        return ResponseEntity.ok(ApiResponse.success("Producto eliminado correctamente"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> searchProduct(@RequestParam String query) {
        List<ProductResponseDTO> results = productService.searchByCode(query);

        String message = results.isEmpty()
                ? "No se encontró el producto ni equivalencias compatibles."
                : "Productos encontrados exitosamente.";

        return ResponseEntity.ok(ApiResponse.success(results, message));
    }

    @PostMapping(value = "/cross-references/import-csv", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<BulkImportResponseDTO>> importCrossReferences(
            @RequestParam("file") MultipartFile file) {

        BulkImportResponseDTO result = productService.importCrossReferencesCsv(file);
        return ResponseEntity.ok(ApiResponse.success(result, "Archivo procesado."));
    }
}