package com.warnes.erp.modules.sales.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.sales.dto.request.PriceListRequestDTO;
import com.warnes.erp.modules.sales.dto.response.PriceListResponseDTO;
import com.warnes.erp.modules.sales.entity.PriceList;
import com.warnes.erp.modules.sales.mapper.PriceListMapper;
import com.warnes.erp.modules.sales.repository.PriceListRepository;
import com.warnes.erp.modules.sales.service.PriceListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

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
        return priceListMapper.toResponseDTO(findEntityByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceListResponseDTO> getAllPriceLists() {
        return priceListRepository.findAll().stream()
                .map(priceListMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PriceListResponseDTO updatePriceList(UUID uuid, PriceListRequestDTO request) {
        PriceList priceList = findEntityByUuid(uuid);

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
        PriceList priceList = findEntityByUuid(uuid);
        // Soft Delete apalancado en BaseEntity
        priceList.setDeletedAt(ZonedDateTime.now());
        priceListRepository.save(priceList);
    }

    private PriceList findEntityByUuid(UUID uuid) {
        return priceListRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada"));
    }
}