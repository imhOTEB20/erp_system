package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.sales.dto.request.SaleCreateRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SalePaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponseDTO>> createSale(@Valid @RequestBody SaleCreateRequestDTO request) {
        SaleResponseDTO response = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Venta creada en estado PENDIENTE. Esperando cobro."));
    }

    @PostMapping("/{uuid}/payments")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> registerPayment(
            @PathVariable UUID uuid,
            @Valid @RequestBody SalePaymentRequestDTO request,
            @RequestParam UUID originWarehouseUuid // Requerido para deducir stock al completar el total
    ) {
        SaleResponseDTO response = saleService.registerPayment(uuid, request, originWarehouseUuid);

        String message = response.status().equals("COMPLETED")
                ? "Total cubierto. Venta completada e inventario deducido."
                : "Pago parcial registrado exitosamente.";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSale(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleByUuid(uuid)));
    }
}