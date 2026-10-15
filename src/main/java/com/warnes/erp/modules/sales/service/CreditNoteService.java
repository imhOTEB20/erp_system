package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.request.CreditNoteRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CreditNoteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CreditNoteService {
    void createCreditNote(CreditNoteRequestDTO request);

    Page<CreditNoteResponseDTO> getCreditNotes(Pageable pageable);
    CreditNoteResponseDTO getCreditNoteByUuid(UUID uuid);
}