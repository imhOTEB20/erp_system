package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.ElementNotDeletedException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.BrandRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BrandResponseDTO;
import com.warnes.erp.modules.catalog.entity.Brand;
import com.warnes.erp.modules.catalog.mapper.BrandMapper;
import com.warnes.erp.modules.catalog.repository.BrandRepository;
import com.warnes.erp.modules.catalog.repository.BrandSpecification;
import com.warnes.erp.modules.catalog.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
        Brand brand = findByUuid(uuid);

        brand.setName(request.name());
        brand.setActive(request.active());

        return brandMapper.toResponseDTO(brandRepository.save(brand));
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponseDTO getBrandByUuid(UUID uuid) {
        Brand brand = findByUuid(uuid);
        return brandMapper.toResponseDTO(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getAllBrandes(Boolean isActive, Boolean includeDeleted) {
        Specification<Brand> filters = BrandSpecification.withDynamicFilters(isActive, includeDeleted);
        return brandRepository.findAll(filters).stream()
                .map(brandMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBrand(UUID uuid) {
        Brand brand = findByUuid(uuid);

        // Soft Delete estructurado
        brand.setDeletedAt(ZonedDateTime.now());
        brand.setActive(false);
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public Boolean toggleAvailability(UUID uuid) {
        Brand brand = findByUuid(uuid);
        brand.setActive(!brand.isActive());

        brandRepository.save(brand);
        return brand.isActive();
    }

    @Override
    @Transactional
    public BrandResponseDTO restoreBrand(UUID uuid) {
        Brand brand = brandRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        if (brand.getDeletedAt() != null) {
            brand.setDeletedAt(null);
            brand.setActive(true);
        } else {
            throw new ElementNotDeletedException("El brand no esta eliminado.");
        }

        return brandMapper.toResponseDTO(
                brandRepository.save(brand)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getAllDeleted() {
        Specification<Brand> filter = BrandSpecification.deleted();
        return brandRepository.findAll(filter).stream()
                .map(brandMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Brand findByUuid(UUID uuid) {
        Specification<Brand> filter = Specification
                .where(BrandSpecification.hasUuid(uuid))
                .and(BrandSpecification.notDeleted());
        return brandRepository.findOne(filter)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
    }
}