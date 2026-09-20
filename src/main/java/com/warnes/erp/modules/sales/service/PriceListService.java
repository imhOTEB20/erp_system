package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.request.PriceListRequestDTO;
import com.warnes.erp.modules.sales.dto.response.PriceListResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PriceListService {
    PriceListResponseDTO createPriceList(PriceListRequestDTO request);
    PriceListResponseDTO getPriceListByUuid(UUID uuid);
    List<PriceListResponseDTO> getAllPriceLists();
    PriceListResponseDTO updatePriceList(UUID uuid, PriceListRequestDTO request);
    void deletePriceList(UUID uuid);
}