package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductUpdateRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BulkImportResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Catálogo - Productos", description = "Gestión de repuestos, servicios y motor de búsqueda con equivalencias (Cross-References)")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Registrar Producto Nuevo")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> create(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Producto creado exitosamente."));
    }

    @Operation(summary = "Consultar Producto (Ficha Completa)")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductByUuid(uuid)));
    }

    @Operation(summary = "Actualizar Producto (Parcial)", description = "Permite modificar atributos específicos del producto sin enviar todo el payload.")
    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> update(
            @PathVariable UUID uuid, @Valid @RequestBody ProductUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(uuid, request), "Producto actualizado exitosamente"));
    }

    @Operation(summary = "Inhabilitar Producto (Soft Delete)")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        productService.deleteProduct(uuid);
        return ResponseEntity.ok(ApiResponse.success("Producto eliminado correctamente"));
    }

    @Operation(summary = "Motor de Búsqueda de Mostrador", description = "Busca repuestos por SKU, Nombre o Códigos Equivalentes de Competencia (Cross-References).")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> searchProduct(
            @Parameter(description = "Término de búsqueda (SKU, nombre o equivalencia)") @RequestParam String query) {
        List<ProductResponseDTO> results = productService.searchByCode(query);
        String message = results.isEmpty() ? "No se encontró el producto." : "Productos encontrados.";
        return ResponseEntity.ok(ApiResponse.success(results, message));
    }

    @Operation(summary = "Importación Masiva (CSV)", description = "Carga masiva de códigos equivalentes para enriquecer la base de datos rápidamente.")
    @PostMapping(value = "/cross-references/import-csv", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<BulkImportResponseDTO>> importCrossReferences(
            @Parameter(description = "Archivo CSV delimitado por comas") @RequestParam("file") MultipartFile file) {
        BulkImportResponseDTO result = productService.importCrossReferencesCsv(file);
        return ResponseEntity.ok(ApiResponse.success(result, "Archivo procesado."));
    }
}