package com.warnes.erp.modules.cash.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.request.CashRegisterRequestDTO;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.service.CashService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}