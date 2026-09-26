package com.warnes.erp.modules.purchases.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.purchases.dto.request.PurchaseCreateRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.PurchaseResponseDTO;
import com.warnes.erp.modules.purchases.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
@Tag(name = "Compras", description = "Ingreso de facturas de proveedores e impacto automático en stock y costos")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "Asentar Factura de Compra", description = "Registra una compra, suma el stock físico al almacén destino y actualiza los precios de costo en el catálogo.")
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseResponseDTO>> create(@Valid @RequestBody PurchaseCreateRequestDTO request) {
        PurchaseResponseDTO response = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Compra registrada. Stock y costos actualizados."));
    }

    @Operation(summary = "Consultar Factura de Compra")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PurchaseResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(purchaseService.getPurchaseByUuid(uuid)));
    }
}