package com.warnes.erp.modules.sales.repository;

import com.warnes.erp.modules.sales.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    Optional<CreditNote> findByUuid(UUID uuid);
    boolean existsByCreditNoteNumberIgnoreCase(String creditNoteNumber);
}