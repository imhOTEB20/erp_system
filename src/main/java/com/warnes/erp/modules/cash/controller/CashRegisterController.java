package com.warnes.erp.modules.cash.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.cash.dto.request.CashRegisterRequestDTO;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.service.CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-registers")
@RequiredArgsConstructor
public class CashRegisterController {

    private final CashService cashService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CashRegisterResponseDTO>> createRegister(@Valid @RequestBody CashRegisterRequestDTO request) {
        CashRegisterResponseDTO response = cashService.createRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Caja creada exitosamente"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')") // Cajeros necesitan leerlas para abrir turno
    public ResponseEntity<ApiResponse<List<CashRegisterResponseDTO>>> getAllRegisters() {
        List<CashRegisterResponseDTO> response = cashService.getAllRegisters();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}