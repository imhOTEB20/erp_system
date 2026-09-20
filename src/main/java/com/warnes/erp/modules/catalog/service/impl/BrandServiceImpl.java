package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.BrandRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
import com.warnes.erp.modules.catalog.entity.Brand;
import com.warnes.erp.modules.catalog.mapper.BrandMapper;
import com.warnes.erp.modules.catalog.repository.BrandRepository;
import com.warnes.erp.modules.catalog.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional
    public BrandResponseDTO createBrand(BrandRequestDTO request) {
        Brand brand = brandMapper.toEntity(request);
        return brandMapper.toResponseDTO(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponseDTO updateBrand(UUID uuid, BrandRequestDTO request) {
        Brand brand = brandRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        brand.setName(request.name());
        brand.setActive(request.active());

        return brandMapper.toResponseDTO(brandRepository.save(brand));
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponseDTO getBrandByUuid(UUID uuid) {
        Brand brand = brandRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
        return brandMapper.toResponseDTO(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getAllBrandes(boolean includeInactive) {
        List<Brand> categories = includeInactive ?
                brandRepository.findAll() :
                brandRepository.findAll().stream().filter(Brand::isActive).toList();

        return categories.stream()
                .map(brandMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBrand(UUID uuid) {
        Brand brand = brandRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        // Soft Delete estructurado
        brand.setDeletedAt(ZonedDateTime.now());
        brand.setActive(false);
        brandRepository.save(brand);
    }
}