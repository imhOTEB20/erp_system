package com.warnes.erp.modules.inventory.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.inventory.dto.request.TransferCreateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.TransferResponseDTO;
import com.warnes.erp.modules.inventory.service.TransferService;
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
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
@Tag(name = "Inventario - Transferencias", description = "Movimientos logísticos entre sucursales y depósitos")
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "Emitir Transferencia", description = "Genera un remito de envío y deduce inmediatamente el stock del almacén de origen.")
    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponseDTO>> createTransfer(@Valid @RequestBody TransferCreateRequestDTO request) {
        TransferResponseDTO response = transferService.createTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transferencia creada y stock deducido del origen"));
    }

    @Operation(summary = "Consultar Transferencia por su codigo de seguimiento")
    @GetMapping("/{tracking_code}")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getTransferByTrackingCode(@PathVariable String trackingCode) {
        return ResponseEntity.ok(ApiResponse.success(transferService.getTransferByTrackingCode(trackingCode)));
    }

    @Operation(summary = "Traer todas las transferencias ordenadas por fecha de creacion.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(transferService.getAll()));
    }

    @Operation(summary = "Completar/Recibir Transferencia", description = "Acusa recibo físico en la sucursal de destino e ingresa la mercadería a su stock local.")
    @PutMapping("/{uuid}/complete")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> completeTransfer(@PathVariable UUID uuid) {
        TransferResponseDTO response = transferService.completeTransfer(uuid);
        return ResponseEntity.ok(ApiResponse.success(response, "Transferencia completada y stock ingresado al destino"));
    }

    @Operation(summary = "Cancelar Transferencia", description = "Anula el envío en tránsito y devuelve la mercadería al almacén de origen.")
    @PutMapping("/{uuid}/cancel")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> cancelTransfer(@PathVariable UUID uuid) {
        TransferResponseDTO response = transferService.cancelTransfer(uuid);
        return ResponseEntity.ok(ApiResponse.success(response, "Transferencia cancelada y stock devuelto al origen"));
    }
}