package com.warnes.erp.modules.cash.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashMovementResponseDTO;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.cash.service.CashShiftService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/cash-shifts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
@Tag(name = "Turnos de Caja", description = "Operativa diaria: aperturas, cierres ciegos, sangrías y movimientos de efectivo")
public class CashShiftController {

    private final CashShiftService cashShiftService;

    @Operation(summary = "Abrir Turno")
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> openShift(@Valid @RequestBody OpenShiftRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cashShiftService.openShift(request), "Turno abierto"));
    }

    @Operation(summary = "Cierre Ciego de Turno")
    @PostMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> closeBlindShift(
            @PathVariable UUID uuid, @Valid @RequestBody CloseShiftRequestDTO request) {
        CashShiftResponseDTO response = cashShiftService.closeShift(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cierre ciego exitoso"));
    }

    @Operation(summary = "Obtener cualquier turno por UUID", description = "Para auditar cierres pasados")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> getShift(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(cashShiftService.getShiftByUuid(uuid)));
    }

    @Operation(summary = "Historial paginado y filtrado")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CashShiftResponseDTO>>> getShifts(
            @RequestParam(required = false) UUID warehouseUuid,
            @RequestParam(required = false) UUID registerUuid,
            @RequestParam(required = false) UUID userUuid,
            @RequestParam(required = false) ShiftStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(cashShiftService.getShifts(warehouseUuid, registerUuid, userUuid, status, from, to, pageable)));
    }

    @Operation(summary = "Mi turno activo (Resolución automática por JWT)")
    @GetMapping("/me/active")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> getMyActiveShift() {
        return ResponseEntity.ok(ApiResponse.success(cashShiftService.getMyActiveShift()));
    }

    @Operation(summary = "Turnos activos (Dashboard)", description = "Visualiza todas las cajas OPEN. Opción a filtrar por sucursal.")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CashShiftResponseDTO>>> getActiveShifts(@RequestParam(required = false) UUID warehouseUuid) {
        return ResponseEntity.ok(ApiResponse.success(cashShiftService.getActiveShifts(warehouseUuid)));
    }

    @Operation(summary = "Detalle de movimientos de un turno específico")
    @GetMapping("/{uuid}/movements")
    public ResponseEntity<ApiResponse<Page<CashMovementResponseDTO>>> getShiftMovements(
            @PathVariable UUID uuid, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(cashShiftService.getShiftMovements(uuid, pageable)));
    }

    @Operation(summary = "Reabrir turno cerrado (Override Administrativo)")
    @PatchMapping("/{uuid}/reopen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> reopenShift(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(cashShiftService.reopenShift(uuid), "Turno reabierto y descuadres revertidos en auditoría."));
    }
    //Metodos anteriores

    @Operation(summary = "Registrar movimiento manual", description = "Ingreso o egreso manual de efectivo que no provenga de una venta (ej. pago a proveedores menores).")
    @PostMapping("/{uuid}/movements")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerMovement(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashMovementRequestDTO request) {
        CashShiftResponseDTO response = cashShiftService.registerMovement(uuid, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Movimiento registrado correctamente en caja"));
    }

    @Operation(summary = "Consultar turno activo por Caja", description = "Devuelve el turno que se encuentra abierto actualmente en una caja específica.")
    @GetMapping("/register/{registerUuid}/active")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> getActiveShift(@PathVariable UUID registerUuid) {
        return ResponseEntity.ok(ApiResponse.success(cashShiftService.getActiveShiftByRegister(registerUuid)));
    }

    @Operation(summary = "Sangría (Retiro de Seguridad)", description = "Retiro parcial de efectivo hacia la caja fuerte por seguridad comercial.")
    @PostMapping("/{uuid}/safe-drop")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerSafeDrop(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashAdjustmentRequestDTO request) {
        CashShiftResponseDTO response = cashShiftService.registerSafeDrop(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sangría de caja registrada. Efectivo deducido."));
    }

    @Operation(summary = "Ingreso de Sencillo", description = "Ingreso de billetes de baja denominación a la gaveta para otorgar vuelto.")
    @PostMapping("/{uuid}/petty-cash")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerPettyCashIn(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashAdjustmentRequestDTO request) {
        CashShiftResponseDTO response = cashShiftService.registerPettyCashIn(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Ingreso de sencillo registrado. Efectivo sumado."));
    }
}