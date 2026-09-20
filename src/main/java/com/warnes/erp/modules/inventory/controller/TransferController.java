package com.warnes.erp.modules.inventory.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.inventory.dto.request.TransferCreateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.TransferResponseDTO;
import com.warnes.erp.modules.inventory.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')") // Aplica a todos los endpoints de esta clase
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponseDTO>> createTransfer(@Valid @RequestBody TransferCreateRequestDTO request) {
        TransferResponseDTO response = transferService.createTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transferencia creada y stock deducido del origen"));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getTransferByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(transferService.getTransferByUuid(uuid)));
    }

    @PutMapping("/{uuid}/complete")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> completeTransfer(@PathVariable UUID uuid) {
        TransferResponseDTO response = transferService.completeTransfer(uuid);
        return ResponseEntity.ok(ApiResponse.success(response, "Transferencia completada y stock ingresado al destino"));
    }

    @PutMapping("/{uuid}/cancel")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> cancelTransfer(@PathVariable UUID uuid) {
        TransferResponseDTO response = transferService.cancelTransfer(uuid);
        return ResponseEntity.ok(ApiResponse.success(response, "Transferencia cancelada y stock devuelto al origen"));
    }
}