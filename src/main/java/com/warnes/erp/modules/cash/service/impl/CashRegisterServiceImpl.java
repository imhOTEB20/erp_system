package com.warnes.erp.modules.cash.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ElementNotDeletedException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;

import com.warnes.erp.modules.cash.entity.CashRegister;
import com.warnes.erp.modules.cash.mapper.CashRegisterMapper;
import com.warnes.erp.modules.cash.repository.CashRegisterRepository;
import com.warnes.erp.modules.cash.repository.CashRegisterSpecification;

import com.warnes.erp.modules.cash.service.CashRegisterService;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
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
public class CashRegisterServiceImpl implements CashRegisterService {

    private final CashRegisterRepository cashRegisterRepository;
    private final CashRegisterMapper cashRegisterMapper;

    private final WarehouseRepository warehouseRepository;


    @Override
    @Transactional
    public CashRegisterResponseDTO createRegister(CashRegisterRequestDTO request) {
        // 1. Buscamos la sucursal
        Warehouse warehouse = warehouseRepository.findByUuid(request.warehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("La sucursal indicada no existe"));

        // 2. Validamos unicidad dentro de esa sucursal
        if (cashRegisterRepository.existsByWarehouse_IdAndNameIgnoreCase(warehouse.getId(), request.name())) {
            throw new BusinessRuleException("Ya existe una caja registrada con ese nombre en esta sucursal");
        }

        // 3. Mapeamos y asociamos
        CashRegister register = cashRegisterMapper.toEntity(request);
        register.setWarehouse(warehouse);

        return cashRegisterMapper.toResponseDTO(cashRegisterRepository.save(register));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashRegisterResponseDTO> getAllRegisters() {
        return cashRegisterRepository.findAll().stream()
                .map(cashRegisterMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashRegisterResponseDTO> getRegistersByWarehouse(UUID warehouseUuid) {
        if (!warehouseRepository.findByUuid(warehouseUuid).isPresent()) {
            throw new ResourceNotFoundException("La sucursal no existe");
        }

        return cashRegisterRepository.findByWarehouse_Uuid(warehouseUuid).stream()
                .map(cashRegisterMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public CashRegisterResponseDTO update(UUID cashRegisterUuid, CashRegisterUpdateRequestDTO request) {
        CashRegister cashRegister = findByUuid(cashRegisterUuid);

        cashRegister.setName(request.name());
        cashRegister.setActive(request.active());

        return cashRegisterMapper.toResponseDTO(cashRegisterRepository.save(cashRegister));
    }

    @Override
    @Transactional(readOnly = true)
    public CashRegisterResponseDTO getCashRegisterByUuid(UUID uuid) {
        return cashRegisterMapper.toResponseDTO(findByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashRegisterResponseDTO> getAllDeleted() {
        Specification<CashRegister> filter = CashRegisterSpecification
                .deleted();
        return cashRegisterRepository.findAll(filter).stream()
                .map(cashRegisterMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        CashRegister cashRegister = findByUuid(uuid);

        //soft delete
        cashRegister.setDeletedAt(ZonedDateTime.now());
        cashRegister.setActive(false);
        cashRegisterRepository.save(cashRegister);
    }

    @Override
    @Transactional
    public CashRegisterResponseDTO restore(UUID uuid) {
        CashRegister cashRegister = cashRegisterRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada."));

        if (cashRegister.getDeletedAt() != null) {
            cashRegister.setDeletedAt(null);
            cashRegister.setActive(true);
        } else {
            throw new ElementNotDeletedException("La caja no esta eliminada.");
        }

        return cashRegisterMapper.toResponseDTO(cashRegisterRepository.save(cashRegister));
    }

    @Override
    @Transactional
    public Boolean toggleAvailability(UUID uuid) {
        CashRegister cashRegister = findByUuid(uuid);
        cashRegister.setActive(!cashRegister.isActive());

        return cashRegisterRepository.save(cashRegister).isActive();
    }

    private CashRegister findByUuid(UUID uuid) {
        Specification<CashRegister> filter = Specification
                .where(CashRegisterSpecification.hasUuid(uuid))
                .and(CashRegisterSpecification.notDeleted());
        return cashRegisterRepository.findOne(filter)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));
    }


}