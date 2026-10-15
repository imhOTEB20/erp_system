package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
import com.warnes.erp.modules.sales.dto.request.PriceListRequestDTO;
import com.warnes.erp.modules.sales.dto.response.PriceListResponseDTO;
import com.warnes.erp.modules.sales.service.PriceListService;
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
@RequestMapping("/api/v1/price-lists")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Exclusivo para gerencia/administradores
@Tag(name = "Ventas - Listas de Precios", description = "Administración de márgenes comerciales para descuentos o recargos automáticos")
public class PriceListController {

    private final PriceListService priceListService;

    @Operation(summary = "Crear Lista de Precios")
    @PostMapping
    public ResponseEntity<ApiResponse<PriceListResponseDTO>> create(@Valid @RequestBody PriceListRequestDTO request) {
        PriceListResponseDTO response = priceListService.createPriceList(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lista de precios creada exitosamente"));
    }

    @Operation(summary = "Listar todas las Listas de Precios")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PriceListResponseDTO>>> getAll(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean includeDeleted
    ) {
        return ResponseEntity.ok(ApiResponse.success(priceListService.getAllPriceLists(isActive, includeDeleted)));
    }

    @Operation(summary = "Consultar Lista de Precios")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PriceListResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(priceListService.getPriceListByUuid(uuid)));
    }

    @Operation(summary = "Actualizar Lista de Precios", description = "Altera el margen que impactará en la próxima venta a los clientes asociados a esta lista.")
    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PriceListResponseDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody PriceListRequestDTO request) {
        PriceListResponseDTO response = priceListService.updatePriceList(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Lista de precios actualizada correctamente"));
    }

    @Operation(summary = "Eliminar Lista de Precios (Soft Delete)")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        priceListService.deletePriceList(uuid);
        return ResponseEntity.ok(ApiResponse.success("Lista de precios inhabilitada (Soft Delete)"));
    }

    @Operation(summary = "Restaura una lista de precios que este eliminada.")
    @PatchMapping("/{uuid}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceListResponseDTO>> restore(
            @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(priceListService.restorePriceList(uuid)));
    }

    @Operation(summary = "Listar todos las marcas eliminadas.")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<PriceListResponseDTO>>> getAllDeleted() {
        return ResponseEntity.ok(ApiResponse.success(priceListService.getAllDeleted()));
    }

    //toggle_availability
    @Operation(summary = "Alternar si el precio de lista esta o no disponible")
    @PatchMapping("/{uuid}/toggle_availability")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID uuid) {
        Boolean newState = priceListService.toggleAvailability(uuid);
        return ResponseEntity.ok(ApiResponse.success(String.format("Precio de lista %s con exito", newState ? "activada" : "desactivada")));
    }
}