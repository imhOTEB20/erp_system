package com.warnes.erp.modules.cash.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;
import com.warnes.erp.modules.cash.service.CashService;
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
@RequestMapping("/api/v1/cash-shifts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
@Tag(name = "Turnos de Caja", description = "Operativa diaria: aperturas, cierres ciegos, sangrías y movimientos de efectivo")
public class CashShiftController {

    private final CashService cashService;

    @Operation(summary = "Abrir Turno", description = "Abre un turno en una caja registradora vinculándolo al usuario actual con un saldo inicial.")
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> openShift(@Valid @RequestBody OpenShiftRequestDTO request) {
        CashShiftResponseDTO response = cashService.openShift(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Turno de caja abierto exitosamente"));
    }

    @Operation(summary = "Cierre Ciego de Turno", description = "Finaliza el turno actual. El cajero declara el efectivo físico y el sistema calcula descuadres automáticamente.")
    @PostMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> closeBlindShift(
            @Parameter(description = "UUID del turno a cerrar") @PathVariable UUID uuid,
            @Valid @RequestBody CloseShiftRequestDTO request) {

        CashShiftResponseDTO response = cashService.closeShift(uuid, request);
        String message = response.discrepancy().compareTo(java.math.BigDecimal.ZERO) == 0
                ? "Cierre ciego exitoso. La caja cuadra perfectamente."
                : "Cierre ciego finalizado con descuadre. Se ha registrado el faltante/sobrante para auditoría.";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @Operation(summary = "Registrar movimiento manual", description = "Ingreso o egreso manual de efectivo que no provenga de una venta (ej. pago a proveedores menores).")
    @PostMapping("/{uuid}/movements")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerMovement(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashMovementRequestDTO request) {
        CashShiftResponseDTO response = cashService.registerMovement(uuid, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Movimiento registrado correctamente en caja"));
    }

    @Operation(summary = "Consultar turno activo por Caja", description = "Devuelve el turno que se encuentra abierto actualmente en una caja específica.")
    @GetMapping("/register/{registerUuid}/active")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> getActiveShift(@PathVariable UUID registerUuid) {
        return ResponseEntity.ok(ApiResponse.success(cashService.getActiveShiftByRegister(registerUuid)));
    }

    @Operation(summary = "Sangría (Retiro de Seguridad)", description = "Retiro parcial de efectivo hacia la caja fuerte por seguridad comercial.")
    @PostMapping("/{uuid}/safe-drop")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerSafeDrop(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashAdjustmentRequestDTO request) {
        CashShiftResponseDTO response = cashService.registerSafeDrop(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sangría de caja registrada. Efectivo deducido."));
    }

    @Operation(summary = "Ingreso de Sencillo", description = "Ingreso de billetes de baja denominación a la gaveta para otorgar vuelto.")
    @PostMapping("/{uuid}/petty-cash")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerPettyCashIn(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashAdjustmentRequestDTO request) {
        CashShiftResponseDTO response = cashService.registerPettyCashIn(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Ingreso de sencillo registrado. Efectivo sumado."));
    }
}