package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.sales.dto.request.CreditNoteRequestDTO;
import com.warnes.erp.modules.sales.service.CreditNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit-notes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCreditNote(@Valid @RequestBody CreditNoteRequestDTO request) {
        creditNoteService.createCreditNote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nota de Crédito generada, stock devuelto y saldos actualizados"));
    }
}