package com.warnes.erp.modules.purchases.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.purchases.dto.request.PurchaseCreateRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.PurchaseResponseDTO;
import com.warnes.erp.modules.purchases.service.PurchaseService;
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
@PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')") // Blindaje a nivel de clase
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseResponseDTO>> create(@Valid @RequestBody PurchaseCreateRequestDTO request) {
        PurchaseResponseDTO response = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Compra registrada. Stock y costos actualizados."));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PurchaseResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(purchaseService.getPurchaseByUuid(uuid)));
    }
}