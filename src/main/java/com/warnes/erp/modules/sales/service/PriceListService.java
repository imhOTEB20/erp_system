package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.request.PriceListRequestDTO;
import com.warnes.erp.modules.sales.dto.response.PriceListResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PriceListService {
    PriceListResponseDTO createPriceList(PriceListRequestDTO request);
    PriceListResponseDTO updatePriceList(UUID uuid, PriceListRequestDTO request);
    PriceListResponseDTO getPriceListByUuid(UUID uuid);
    List<PriceListResponseDTO> getAllPriceLists(Boolean isActive, Boolean includeDeleted);
    void deletePriceList(UUID uuid);
    Boolean toggleAvailability(UUID uuid);
    PriceListResponseDTO restorePriceList(UUID uuid);
    List<PriceListResponseDTO> getAllDeleted();
}