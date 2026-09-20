package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.request.CreditNoteRequestDTO;
import java.util.UUID;

public interface CreditNoteService {
    void createCreditNote(CreditNoteRequestDTO request);
}