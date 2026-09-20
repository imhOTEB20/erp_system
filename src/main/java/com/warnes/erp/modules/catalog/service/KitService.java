package com.warnes.erp.modules.catalog.service;

import com.warnes.erp.modules.catalog.dto.request.KitItemRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.KitItemResponseDTO;

import java.util.List;
import java.util.UUID;

public interface KitService {
    List<KitItemResponseDTO> addOrUpdateKitItem(UUID parentUuid, KitItemRequestDTO request);
    void removeKitItem(UUID parentUuid, UUID childUuid);
    List<KitItemResponseDTO> getKitItems(UUID parentUuid);
}