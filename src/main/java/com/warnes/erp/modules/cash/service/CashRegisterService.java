package com.warnes.erp.modules.cash.service;

import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CashRegisterService {
    CashRegisterResponseDTO createRegister(CashRegisterRequestDTO request);
    List<CashRegisterResponseDTO> getAllRegisters();
    List<CashRegisterResponseDTO> getRegistersByWarehouse(UUID warehouseUuid);

    //metodos nuevos
    CashRegisterResponseDTO update(UUID cashRegisterUuid, CashRegisterUpdateRequestDTO request);
    CashRegisterResponseDTO getCashRegisterByUuid(UUID uuid);
    List<CashRegisterResponseDTO> getAllDeleted();
    void delete(UUID uuid);
    CashRegisterResponseDTO restore(UUID uuid);
    Boolean toggleAvailability(UUID uuid);
}