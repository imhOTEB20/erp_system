package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductUpdateRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BulkImportResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductSimpleResponseDTO;
import com.warnes.erp.modules.catalog.service.ProductExportService;
import com.warnes.erp.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final ProductExportService productExportService;

    @Operation(summary = "Registrar Producto Nuevo")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> create(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Producto creado exitosamente."));
    }

    @Operation(summary = "Listado paginado de productos", description = "Devuelve el catálogo de productos activos. Soporta filtros opcionales por categoría, marca, kits y servicios.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getProducts(
            @Parameter(description = "UUID de la Categoría") @RequestParam(required = false) UUID categoryUuid,
            @Parameter(description = "UUID de la Marca") @RequestParam(required = false) UUID brandUuid,
            @Parameter(description = "Filtrar solo Kits") @RequestParam(required = false) Boolean isKit,
            @Parameter(description = "Filtrar solo Servicios") @RequestParam(required = false) Boolean isService,
            @Parameter(description = "Filtrar solo Activos") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Agrega los productos eliminados a la lista") @RequestParam(required = false) Boolean includeDeleted,
            @ParameterObject Pageable pageable
    ) {

        Page<ProductResponseDTO> response = productService.getProducts(categoryUuid, brandUuid, isKit, isService, isActive, includeDeleted, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Catálogo recuperado exitosamente.")
        );
    }

    @Operation(summary = "Listar todos los productos eliminados.")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAllDeleted() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllDeleted()));
    }

    @Operation(summary = "Listar nombres e identificador.", description = "Lista los nombre y los uuid de todos los productos.")
    @GetMapping("/list_names")
    public ResponseEntity<ApiResponse<List<ProductSimpleResponseDTO>>> getListProductNames(
            @Parameter(description = "Agrega los productos eliminados a la lista") @RequestParam(required = false) Boolean includeDeleted
    ) {
        return ResponseEntity.ok(ApiResponse.success(productService.getListProductNames(includeDeleted)));
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

    //toggle_availability
    @Operation(summary = "Alternar si el producto esta o no disponible")
    @PatchMapping("/{uuid}/toggle_availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID uuid) {
        Boolean newState = productService.toggleAvailability(uuid);
        return ResponseEntity.ok(ApiResponse.success(String.format("Producto %s con exito", newState ? "activada" : "desactivada")));
    }

    @Operation(summary = "Restaura un producto que este eliminado.")
    @PatchMapping("/{uuid}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> restore(
            @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(productService.restoreProduct(uuid)));
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
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> searchProduct(
            @Parameter(description = "UUID de la Categoría") @RequestParam(required = false) UUID categoryUuid,
            @Parameter(description = "UUID de la Marca") @RequestParam(required = false) UUID brandUuid,
            @Parameter(description = "Término de búsqueda (SKU, nombre o equivalencia)") @RequestParam String query,
            @Parameter(description = "Filtrar solo Kits") @RequestParam(required = false) Boolean isKit,
            @Parameter(description = "Filtrar solo Servicios") @RequestParam(required = false) Boolean isService,
            @Parameter(description = "Filtrar solo Activos") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Agrega los productos eliminados a la lista") @RequestParam(required = false) Boolean includeDeleted,
            @ParameterObject Pageable pageable) {
        Page<ProductResponseDTO> results = productService.searchByCode(query, categoryUuid, brandUuid, isKit, isService, isActive, includeDeleted, pageable);
        String message = results.isEmpty() ? "No se encontró el producto." : "Productos encontrados.";
        return ResponseEntity.ok(
                ApiResponse.success(results, message)
        );
    }

    @Operation(summary = "Importación Masiva (CSV)", description = "Carga masiva de códigos equivalentes para enriquecer la base de datos rápidamente.")
    @PostMapping(value = "/cross-references/import-csv", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<BulkImportResponseDTO>> importCrossReferences(
            @Parameter(description = "Archivo CSV delimitado por comas") @RequestParam("file") MultipartFile file) {
        BulkImportResponseDTO result = productService.importCrossReferencesCsv(file);
        return ResponseEntity.ok(ApiResponse.success(result, "Archivo procesado."));
    }

    //EXPORTS
    @Operation(summary = "Exportar Catálogo a Excel", description = "Descarga la lista completa de productos activos en formato .xlsx.")
    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<byte[]> exportProductsToExcel() {

        // 1. Obtenemos los DTOs de la base de datos a través del servicio principal
        List<ProductResponseDTO> products = productService.getProductsForExport();

        // 2. Transformamos los DTOs en un array de bytes (el archivo Excel)
        byte[] excelContent = productExportService.exportProductsToExcel(products);

        // 3. Configuramos los headers para forzar la descarga
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "catalogo_repuestos.xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }
}