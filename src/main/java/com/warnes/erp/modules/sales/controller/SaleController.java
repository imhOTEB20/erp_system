package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.sales.dto.request.SaleCreateRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SalePaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Ventas de Mostrador", description = "Endpoints para la creación, consulta y cobro de facturas")
public class SaleController {

    private final SaleService saleService;

    @Operation(summary = "Registrar nueva venta", description = "Crea una venta en estado PENDIENTE. Verifica disponibilidad pero no deduce stock físico hasta que se registre el pago.")
    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponseDTO>> createSale(@Valid @RequestBody SaleCreateRequestDTO request) {
        SaleResponseDTO response = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Venta creada en estado PENDIENTE. Esperando cobro."));
    }

    @Operation(summary = "Registrar cobro total o parcial", description = "Aplica un pago a la factura. Si cubre el total, deduce el stock físico del almacén y completa la venta.")
    @PostMapping("/{uuid}/payments")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> registerPayment(
            @Parameter(description = "UUID de la venta a cobrar", required = true) @PathVariable UUID uuid,
            @Valid @RequestBody SalePaymentRequestDTO request,
            @Parameter(description = "UUID del almacén origen para deducir stock si se completa la venta", required = true) @RequestParam UUID originWarehouseUuid
    ) {
        SaleResponseDTO response = saleService.registerPayment(uuid, request, originWarehouseUuid);

        String message = response.status().equals("COMPLETED")
                ? "Total cubierto. Venta completada e inventario deducido."
                : "Pago parcial registrado exitosamente.";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @Operation(summary = "Consultar detalle de venta", description = "Retorna la cabecera, ítems, impuestos y pagos asociados a una factura específica.")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSale(
            @Parameter(description = "UUID de la venta", required = true) @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleByUuid(uuid)));
    }
}