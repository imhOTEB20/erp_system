package com.warnes.erp.modules.sales.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ElementNotDeletedException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.entity.Brand;
import com.warnes.erp.modules.sales.dto.request.PriceListRequestDTO;
import com.warnes.erp.modules.sales.dto.response.PriceListResponseDTO;
import com.warnes.erp.modules.sales.entity.PriceList;
import com.warnes.erp.modules.sales.mapper.PriceListMapper;
import com.warnes.erp.modules.sales.repository.PriceListRepository;
import com.warnes.erp.modules.sales.repository.PriceListSpecification;
import com.warnes.erp.modules.sales.service.PriceListService;
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
public class PriceListServiceImpl implements PriceListService {

    private final PriceListRepository priceListRepository;
    private final PriceListMapper priceListMapper;

    @Override
    @Transactional
    public PriceListResponseDTO createPriceList(PriceListRequestDTO request) {
        if (priceListRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessRuleException("Ya existe una lista de precios con el nombre ingresado");
        }

        PriceList priceList = priceListMapper.toEntity(request);
        return priceListMapper.toResponseDTO(priceListRepository.save(priceList));
    }

    @Override
    @Transactional(readOnly = true)
    public PriceListResponseDTO getPriceListByUuid(UUID uuid) {
        return priceListMapper.toResponseDTO(findByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceListResponseDTO> getAllPriceLists(Boolean isActive, Boolean includeDeleted) {
        Specification<PriceList> filters = PriceListSpecification.withDynamicFilters(isActive, includeDeleted);
        return priceListRepository.findAll(filters).stream()
                .map(priceListMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PriceListResponseDTO updatePriceList(UUID uuid, PriceListRequestDTO request) {
        PriceList priceList = findByUuid(uuid);

        if (!priceList.getName().equalsIgnoreCase(request.name()) &&
                priceListRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessRuleException("El nuevo nombre ya está en uso por otra lista de precios");
        }

        priceListMapper.updateEntityFromDto(request, priceList);
        return priceListMapper.toResponseDTO(priceListRepository.save(priceList));
    }

    @Override
    @Transactional
    public void deletePriceList(UUID uuid) {
        PriceList priceList = findByUuid(uuid);
        // Soft Delete apalancado en BaseEntity
        priceList.setDeletedAt(ZonedDateTime.now());
        priceList.setActive(false);
        priceListRepository.save(priceList);
    }

    @Override
    public Boolean toggleAvailability(UUID uuid) {
        PriceList priceList = findByUuid(uuid);
        priceList.setActive(!priceList.isActive());

        priceListRepository.save(priceList);
        return priceList.isActive();
    }

    @Override
    public PriceListResponseDTO restorePriceList(UUID uuid) {
        PriceList priceList = priceListRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada"));

        if (priceList.getDeletedAt() != null) {
            priceList.setDeletedAt(null);
            priceList.setActive(true);
        } else {
            throw new ElementNotDeletedException("Lista de precios no esta eliminada.");
        }

        return priceListMapper.toResponseDTO(
                priceListRepository.save(priceList)
        );
    }

    @Override
    public List<PriceListResponseDTO> getAllDeleted() {
        Specification<PriceList> filter = PriceListSpecification.deleted();
        return priceListRepository.findAll(filter).stream()
                .map(priceListMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private PriceList findByUuid(UUID uuid) {
        Specification<PriceList> filter = Specification
                .where(PriceListSpecification.hasUuid(uuid))
                .and(PriceListSpecification.notDeleted());
        return priceListRepository.findOne(filter)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada"));
    }
}