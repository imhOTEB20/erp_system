package com.warnes.erp.modules.cash.repository;

import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CashShiftRepository extends JpaRepository<CashShift, Long> {
    Optional<CashShift> findByUuid(UUID uuid);
    Optional<CashShift> findByCashRegisterIdAndStatus(Long cashRegisterId, ShiftStatus status);
    // Buscar el turno abierto del usuario logueado
    Optional<CashShift> findByUserIdAndStatus(Long userId, ShiftStatus status);
}