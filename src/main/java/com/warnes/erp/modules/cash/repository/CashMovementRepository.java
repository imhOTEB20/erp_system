package com.warnes.erp.modules.cash.repository;

import com.warnes.erp.modules.cash.entity.CashMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
    Page<CashMovement> findByCashShiftIdOrderByCreatedAtDesc(Long cashShiftId, Pageable pageable);
}