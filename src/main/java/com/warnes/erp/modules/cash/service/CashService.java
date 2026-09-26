package com.warnes.erp.modules.cash.service;

import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CashService {
    CashRegisterResponseDTO createRegister(CashRegisterRequestDTO request);
    List<CashRegisterResponseDTO> getAllRegisters();

    CashShiftResponseDTO openShift(OpenShiftRequestDTO request);
    CashShiftResponseDTO closeShift(UUID shiftUuid, CloseShiftRequestDTO request);
    CashShiftResponseDTO registerMovement(UUID shiftUuid, CashMovementRequestDTO request);
    CashShiftResponseDTO getActiveShiftByRegister(UUID registerUuid);
    CashShiftResponseDTO registerSafeDrop(UUID shiftUuid, CashAdjustmentRequestDTO request);
    CashShiftResponseDTO registerPettyCashIn(UUID shiftUuid, CashAdjustmentRequestDTO request);
    List<CashRegisterResponseDTO> getRegistersByWarehouse(UUID warehouseUuid);
}