package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<PriceListResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(priceListService.getAllPriceLists()));
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
}