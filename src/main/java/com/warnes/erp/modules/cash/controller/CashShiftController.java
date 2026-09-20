package com.warnes.erp.modules.cash.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.request.CashAdjustmentRequestDTO;
import com.warnes.erp.modules.cash.dto.request.CashMovementRequestDTO;
import com.warnes.erp.modules.cash.dto.request.CloseShiftRequestDTO;
import com.warnes.erp.modules.cash.dto.request.OpenShiftRequestDTO;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;
import com.warnes.erp.modules.cash.service.CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cash-shifts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class CashShiftController {

    private final CashService cashService;

    @PostMapping("/open")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> openShift(@Valid @RequestBody OpenShiftRequestDTO request) {
        CashShiftResponseDTO response = cashService.openShift(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Turno de caja abierto exitosamente"));
    }

    @PostMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> closeBlindShift(
            @PathVariable UUID uuid,
            @Valid @RequestBody CloseShiftRequestDTO request) {

        CashShiftResponseDTO response = cashService.closeShift(uuid, request);

        String message = response.discrepancy().compareTo(java.math.BigDecimal.ZERO) == 0
                ? "Cierre ciego exitoso. La caja cuadra perfectamente."
                : "Cierre ciego finalizado con descuadre. Se ha registrado el faltante/sobrante para auditoría.";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @PostMapping("/{uuid}/movements")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerMovement(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashMovementRequestDTO request) {
        CashShiftResponseDTO response = cashService.registerMovement(uuid, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Movimiento registrado correctamente en caja"));
    }

    @GetMapping("/register/{registerUuid}/active")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> getActiveShift(@PathVariable UUID registerUuid) {
        CashShiftResponseDTO response = cashService.getActiveShiftByRegister(registerUuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{uuid}/safe-drop")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerSafeDrop(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashAdjustmentRequestDTO request) {
        CashShiftResponseDTO response = cashService.registerSafeDrop(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sangría de caja registrada. Efectivo deducido del balance físico esperado."));
    }

    @PostMapping("/{uuid}/petty-cash")
    public ResponseEntity<ApiResponse<CashShiftResponseDTO>> registerPettyCashIn(
            @PathVariable UUID uuid,
            @Valid @RequestBody CashAdjustmentRequestDTO request) {
        CashShiftResponseDTO response = cashService.registerPettyCashIn(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Ingreso de sencillo registrado. Efectivo sumado al balance físico esperado."));
    }
}