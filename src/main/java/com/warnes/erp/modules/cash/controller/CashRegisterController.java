package com.warnes.erp.modules.cash.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.request.CashRegisterRequestDTO;
import com.warnes.erp.modules.cash.dto.request.CashRegisterUpdateRequestDTO;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.service.CashRegisterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/cash-registers")
@RequiredArgsConstructor
@Tag(name = "Cajas Registradoras", description = "Administración de estaciones físicas de cobro")
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    @Operation(summary = "Crear nueva caja física", description = "Da de alta un punto de cobro. Exclusivo para administradores.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> createRegister(@Valid @RequestBody CashRegisterRequestDTO request) {
        CashRegisterResponseDTO response = cashRegisterService.createRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Caja creada exitosamente"));
    }

    @Operation(summary = "Listar cajas", description = "Devuelve todas las estaciones de caja del sistema para poder aperturar turnos.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<CashRegisterResponseDTO>>> getAllRegisters(
            @Parameter(description = "UUID de la sucursal") @RequestParam(required = false) UUID warehouseUuid,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean includeDeleted
    ) {
        return ResponseEntity.ok(ApiResponse.success(cashRegisterService.getAllRegisters(warehouseUuid, isActive, includeDeleted)));
    }

    //nuevos endpoints necesarios.
    @Operation(summary = "Actualizar datos de una caja regitradora.")
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> update(
            @PathVariable UUID uuid, @Valid @RequestBody CashRegisterUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(cashRegisterService.update(uuid, request), "Caja registradora actualizada."));
    }

    @Operation(summary = "Consultar Caja Registradora por UUID")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> get(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(cashRegisterService.getCashRegisterByUuid(uuid)));
    }

    @Operation(summary = "Listar todas las cajas registradoras eliminadas.")
    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CashRegisterResponseDTO>>> getAllDeleted() {
        return ResponseEntity.ok(ApiResponse.success(cashRegisterService.getAllDeleted()));
    }

    @Operation(summary = "Eliminar Caja Registradora", description = "Ejecuta un Soft Delete sobre el una caja registradora.")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        cashRegisterService.delete(uuid);
        return ResponseEntity.ok(ApiResponse.success("Caja eliminada (Soft Delete)"));
    }

    @Operation(summary = "Restaura una caja que este eliminada.")
    @PatchMapping("/{uuid}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> restore(
            @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(cashRegisterService.restore(uuid)));
    }

    //toggle_availability
    @Operation(summary = "Alternar si la caja esta o no disponible.")
    @PatchMapping("/{uuid}/toggle_availability")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID uuid) {
        Boolean newState = cashRegisterService.toggleAvailability(uuid);
        return ResponseEntity.ok(ApiResponse.success(String.format("Caja %s con exito", newState ? "activada" : "desactivada")));
    }
}