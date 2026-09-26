package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.TaxTypeRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.TaxTypeResponseDTO;
import com.warnes.erp.modules.catalog.entity.TaxType;
import com.warnes.erp.modules.catalog.mapper.TaxTypeMapper;
import com.warnes.erp.modules.catalog.repository.TaxTypeRepository;
import com.warnes.erp.modules.catalog.service.TaxTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxTypeServiceImpl implements TaxTypeService {

    private final TaxTypeRepository taxTypeRepository;
    private final TaxTypeMapper taxTypeMapper;

    @Override
    @Transactional
    public TaxTypeResponseDTO createTaxType(TaxTypeRequestDTO request) {
        TaxType taxType = taxTypeMapper.toEntity(request);
        return taxTypeMapper.toResponseDTO(taxTypeRepository.save(taxType));
    }

    @Override
    @Transactional
    public TaxTypeResponseDTO updateTaxType(UUID uuid, TaxTypeRequestDTO request) {
        TaxType taxType = taxTypeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de impuesto no encontrado"));

        taxType.setName(request.name());
        taxType.setDefaultRate(request.defaultRate());
        taxType.setActive(request.active());

        return taxTypeMapper.toResponseDTO(taxTypeRepository.save(taxType));
    }

    @Override
    @Transactional(readOnly = true)
    public TaxTypeResponseDTO getTaxTypeByUuid(UUID uuid) {
        TaxType taxType = taxTypeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de impuesto no encontrado"));
        return taxTypeMapper.toResponseDTO(taxType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxTypeResponseDTO> getAllTaxTypes(boolean includeInactive) {
        List<TaxType> taxes = includeInactive ?
                taxTypeRepository.findAll() :
                taxTypeRepository.findAll().stream().filter(TaxType::isActive).toList();

        return taxes.stream()
                .map(taxTypeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTaxType(UUID uuid) {
        TaxType taxType = taxTypeRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de impuesto no encontrado"));

        // Al no existir columna 'deleted_at', realizamos un Soft Delete lógico mediante el estado.
        taxType.setActive(false);
        taxType.setDeletedAt(ZonedDateTime.now());
        taxTypeRepository.save(taxType);
    }
}