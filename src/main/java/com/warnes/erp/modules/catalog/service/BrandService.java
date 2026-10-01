package com.warnes.erp.modules.catalog.service;

import com.warnes.erp.modules.catalog.dto.request.BrandRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BrandService {
    BrandResponseDTO createBrand(BrandRequestDTO request);
    BrandResponseDTO updateBrand(UUID uuid, BrandRequestDTO request);
    BrandResponseDTO getBrandByUuid(UUID uuid);
    List<BrandResponseDTO> getAllBrandes(Boolean isActive, Boolean includeDeleted);
    void deleteBrand(UUID uuid);
    Boolean toggleAvailability(UUID uuid);
    BrandResponseDTO restoreBrand(UUID uuid);
    List<BrandResponseDTO> getAllDeleted();
}