package com.warnes.erp.modules.inventory.service;

import com.warnes.erp.modules.inventory.dto.request.TransferCreateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.TransferResponseDTO;

import java.util.UUID;

public interface TransferService {
    TransferResponseDTO createTransfer(TransferCreateRequestDTO request);
    TransferResponseDTO getTransferByUuid(UUID uuid);
    TransferResponseDTO completeTransfer(UUID uuid);
    TransferResponseDTO cancelTransfer(UUID uuid);
}