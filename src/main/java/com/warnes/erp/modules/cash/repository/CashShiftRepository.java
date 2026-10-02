package com.warnes.erp.modules.cash.repository;

import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashShiftRepository extends JpaRepository<CashShift, Long>, JpaSpecificationExecutor<CashShift> {
    Optional<CashShift> findByUuid(UUID uuid);
    Optional<CashShift> findByCashRegisterIdAndStatus(Long cashRegisterId, ShiftStatus status);
    Optional<CashShift> findByUserIdAndStatus(Long userId, ShiftStatus status);
    List<CashShift> findByStatusAndOpenedAtBefore(ShiftStatus status, ZonedDateTime threshold);

    // Obtener el turno más reciente de una caja para validar la reapertura
    Optional<CashShift> findFirstByCashRegisterIdOrderByOpenedAtDesc(Long cashRegisterId);

    // Obtener turnos abiertos en una sucursal para el dashboard
    @Query("SELECT cs FROM CashShift cs WHERE cs.status = :status AND (:warehouseUuid IS NULL OR cs.cashRegister.warehouse.uuid = :warehouseUuid)")
    List<CashShift> findActiveShiftsByWarehouse(@Param("status") ShiftStatus status, @Param("warehouseUuid") UUID warehouseUuid);
}