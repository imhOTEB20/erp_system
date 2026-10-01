package com.warnes.erp.modules.cash.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.request.CashRegisterRequestDTO;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.service.CashService;
import com.warnes.erp.modules.catalog.dto.request.BrandRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
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
@RequestMapping("/api/v1/cash-registers")
@RequiredArgsConstructor
@Tag(name = "Cajas Registradoras", description = "Administración de estaciones físicas de cobro")
public class CashRegisterController {

    private final CashService cashService;

    @Operation(summary = "Crear nueva caja física", description = "Da de alta un punto de cobro. Exclusivo para administradores.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> createRegister(@Valid @RequestBody CashRegisterRequestDTO request) {
        CashRegisterResponseDTO response = cashService.createRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Caja creada exitosamente"));
    }

    @Operation(summary = "Listar cajas", description = "Devuelve todas las estaciones de caja del sistema para poder aperturar turnos.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<CashRegisterResponseDTO>>> getAllRegisters() {
        return ResponseEntity.ok(ApiResponse.success(cashService.getAllRegisters()));
    }

    //nuevos endpoints necesarios.
    @Operation(summary = "Actualizar datos de una caja regitradora.")
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> update(
            @PathVariable UUID uuid, @Valid @RequestBody CashRegisterRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(cashService.update(uuid, request), "Caja registradora actualizada."));
    }

    @Operation(summary = "Consultar Caja Registradora por UUID")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> get(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(cashService.getCashRegisterByUuid(uuid)));
    }

    @Operation(summary = "Listar todas las cajas registradoras eliminadas.")
    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CashRegisterResponseDTO>>> getAllDeleted() {
        return ResponseEntity.ok(ApiResponse.success(cashService.getAllDeleted()));
    }

    @Operation(summary = "Eliminar Caja Registradora", description = "Ejecuta un Soft Delete sobre el una caja registradora.")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        cashService.delete(uuid);
        return ResponseEntity.ok(ApiResponse.success("Caja eliminada (Soft Delete)"));
    }

    @Operation(summary = "Restaura una caja que este eliminada.")
    @PatchMapping("/{uuid}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> restore(
            @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(cashService.restore(uuid)));
    }

    //toggle_availability
    @Operation(summary = "Alternar si la caja esta o no disponible.")
    @PatchMapping("/{uuid}/toggle_availability")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID uuid) {
        Boolean newState = cashService.toggleAvailability(uuid);
        return ResponseEntity.ok(ApiResponse.success(String.format("Caja %s con exito", newState ? "activada" : "desactivada")));
    }
}