package com.warnes.erp.modules.purchases.service;

import com.warnes.erp.modules.purchases.dto.request.PurchaseCreateRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.PurchaseResponseDTO;

import java.util.UUID;

public interface PurchaseService {
    PurchaseResponseDTO createPurchase(PurchaseCreateRequestDTO request);
    PurchaseResponseDTO getPurchaseByUuid(UUID uuid);
}