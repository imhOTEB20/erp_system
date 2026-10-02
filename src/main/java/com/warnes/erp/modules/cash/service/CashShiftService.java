package com.warnes.erp.modules.cash.service;

import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashMovementResponseDTO;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface CashShiftService {
    CashShiftResponseDTO openShift(OpenShiftRequestDTO request);
    CashShiftResponseDTO closeShift(UUID shiftUuid, CloseShiftRequestDTO request);
    CashShiftResponseDTO registerMovement(UUID shiftUuid, CashMovementRequestDTO request);
    CashShiftResponseDTO getActiveShiftByRegister(UUID registerUuid);
    CashShiftResponseDTO registerSafeDrop(UUID shiftUuid, CashAdjustmentRequestDTO request);
    CashShiftResponseDTO registerPettyCashIn(UUID shiftUuid, CashAdjustmentRequestDTO request);

    // Nuevas demandas
    CashShiftResponseDTO getShiftByUuid(UUID uuid);
    Page<CashShiftResponseDTO> getShifts(UUID warehouseUuid, UUID registerUuid, UUID userUuid, ShiftStatus status, ZonedDateTime from, ZonedDateTime to, Pageable pageable);
    CashShiftResponseDTO getMyActiveShift();
    List<CashShiftResponseDTO> getActiveShifts(UUID warehouseUuid);
    Page<CashMovementResponseDTO> getShiftMovements(UUID shiftUuid, Pageable pageable);
    CashShiftResponseDTO reopenShift(UUID shiftUuid);
}