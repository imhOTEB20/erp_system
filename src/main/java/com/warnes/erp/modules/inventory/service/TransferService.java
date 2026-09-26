package com.warnes.erp.modules.inventory.service;

import com.warnes.erp.modules.inventory.dto.request.TransferCreateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.TransferResponseDTO;
import com.warnes.erp.modules.inventory.entity.Transfer;

import java.util.List;
import java.util.UUID;

public interface TransferService {
    TransferResponseDTO createTransfer(TransferCreateRequestDTO request);
    TransferResponseDTO getTransferByUuid(UUID uuid);
    TransferResponseDTO getTransferByTrackingCode(String trackingCode);
    List<TransferResponseDTO> getAll();
    TransferResponseDTO completeTransfer(UUID uuid);
    TransferResponseDTO cancelTransfer(UUID uuid);
}