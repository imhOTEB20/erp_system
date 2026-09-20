package com.warnes.erp.modules.sales.repository;

import com.warnes.erp.modules.sales.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    boolean existsByCreditNoteNumberIgnoreCase(String creditNoteNumber);
}