package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.request.TaxTypeRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.CategoryResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.TaxTypeResponseDTO;
import com.warnes.erp.modules.catalog.service.TaxTypeService;
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
@RequestMapping("/api/v1/tax-types")
@RequiredArgsConstructor
@Tag(name = "Catálogo - Impuestos", description = "Gestión de tasas impositivas y alícuotas")
public class TaxTypeController {

    private final TaxTypeService taxTypeService;

    @Operation(summary = "Crear Impuesto", description = "Registra un nuevo tipo de gravamen (Ej: IVA 21%). Exclusivo ADMIN.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaxTypeResponseDTO>> createTaxType(@Valid @RequestBody TaxTypeRequestDTO request) {
        TaxTypeResponseDTO response = taxTypeService.createTaxType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tipo de impuesto creado exitosamente"));
    }

    @Operation(summary = "Actualizar Impuesto")
    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaxTypeResponseDTO>> updateTaxType(
            @PathVariable UUID uuid,
            @Valid @RequestBody TaxTypeRequestDTO request) {
        TaxTypeResponseDTO response = taxTypeService.updateTaxType(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tipo de impuesto actualizado"));
    }

    @Operation(summary = "Consultar Impuesto")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TaxTypeResponseDTO>> getTaxType(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(taxTypeService.getTaxTypeByUuid(uuid)));
    }

    @Operation(summary = "Listar todos los impuestos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxTypeResponseDTO>>> getAllTaxTypes(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean includeDeleted) {
        return ResponseEntity.ok(ApiResponse.success(taxTypeService.getAllTaxTypes(isActive, includeDeleted)));
    }

    @Operation(summary = "Restaura un impuesto que este eliminado.")
    @PatchMapping("/{uuid}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaxTypeResponseDTO>> restore(
            @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(taxTypeService.restoreTaxType(uuid)));
    }

    @Operation(summary = "Listar todos los impuestos eliminados.")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<TaxTypeResponseDTO>>> getAllDeleted() {
        return ResponseEntity.ok(ApiResponse.success(taxTypeService.getAllDeleted()));
    }

    //toggle_availability
    @Operation(summary = "Alternar si la marca esta o no disponible")
    @PatchMapping("/{uuid}/toggle_availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID uuid) {
        Boolean newState = taxTypeService.toggleAvailability(uuid);
        return ResponseEntity.ok(ApiResponse.success(String.format("Impuesto %s con exito", newState ? "activado" : "desactivado")));
    }

    @Operation(summary = "Eliminar Impuesto", description = "Oculta el impuesto de futuras operaciones (Soft Delete).")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTaxType(@PathVariable UUID uuid) {
        taxTypeService.deleteTaxType(uuid);
        return ResponseEntity.ok(ApiResponse.success("Impuesto inhabilitado exitosamente"));
    }
}