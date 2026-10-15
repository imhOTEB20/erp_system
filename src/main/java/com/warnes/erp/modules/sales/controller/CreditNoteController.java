package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.sales.dto.request.CreditNoteRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CreditNoteResponseDTO;
import com.warnes.erp.modules.sales.service.CreditNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit-notes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
@Tag(name = "Ventas - Notas de Crédito", description = "Manejo de devoluciones de mercadería, reversión contable e impacto de stock")
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @Operation(summary = "Emitir Nota de Crédito", description = "Genera un comprobante de anulación, retorna el stock físico al almacén y acredita el dinero en la caja o cuenta corriente.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCreditNote(@Valid @RequestBody CreditNoteRequestDTO request) {
        creditNoteService.createCreditNote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nota de Crédito generada, stock devuelto y saldos actualizados"));
    }

    @Operation(summary = "Listar Notas de Crédito (Paginado)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CreditNoteResponseDTO>>> getCreditNotes(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.getCreditNotes(pageable)));
    }

    @Operation(summary = "Consultar Nota de Crédito por UUID")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CreditNoteResponseDTO>> getCreditNote(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.getCreditNoteByUuid(uuid)));
    }
}