package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.sales.dto.request.SaleCheckoutRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SaleCreateRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SalePaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import com.warnes.erp.modules.sales.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
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

    @Operation(summary = "Venta Mostrador (One-Step Checkout)", description = "Crea la venta, aplica cobros mixtos y deduce stock en una sola transacción ACID. Ideal para agilidad en locales físicos.")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> checkout(@Valid @RequestBody SaleCheckoutRequestDTO request) {
        SaleResponseDTO response = saleService.checkout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transacción completada: Venta registrada y stock deducido exitosamente."));
    }

    @Operation(summary = "Registrar cobros a venta pendiente", description = "Soporta cobros mixtos simultáneos (ej: Efectivo + Tarjeta) sobre una reserva previa (E-Commerce / B2B).")
    @PostMapping("/{uuid}/payments")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> registerPayments(
            @Parameter(description = "UUID de la venta a cobrar", required = true) @PathVariable UUID uuid,
            @Valid @RequestBody List<SalePaymentRequestDTO> requests,
            @Parameter(description = "UUID del almacén (Obligatorio para B2B sin caja asociada)") @RequestParam(required = false) UUID originWarehouseUuid
    ) {
        SaleResponseDTO response = saleService.registerPayments(uuid, requests, originWarehouseUuid);
        String message = response.status().equals("COMPLETED")
                ? "Total cubierto. Venta completada e inventario deducido."
                : "Pagos parciales registrados exitosamente.";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @Operation(summary = "Consultar detalle de venta", description = "Retorna la cabecera, ítems, impuestos y pagos asociados a una factura específica.")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSale(
            @Parameter(description = "UUID de la venta", required = true) @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleByUuid(uuid)));
    }

    @Operation(summary = "Listado paginado de ventas", description = "Filtro dinámico de histórico de ventas")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SaleResponseDTO>>> getSales(
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false) UUID customerUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSales(status, customerUuid, from, to, pageable)));
    }

    @Operation(summary = "Buscar venta por N° Factura")
    @GetMapping("/by-invoice/{invoiceNumber}")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSaleByInvoice(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleByInvoiceNumber(invoiceNumber)));
    }
}