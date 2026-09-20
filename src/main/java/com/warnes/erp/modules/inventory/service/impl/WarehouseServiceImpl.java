package com.warnes.erp.modules.inventory.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseResponseDTO;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.mapper.WarehouseMapper;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request) {
        if (warehouseRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessRuleException("Ya existe un almacén registrado con el código ingresado");
        }

        Warehouse warehouse = warehouseMapper.toEntity(request);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        return warehouseMapper.toResponseDTO(savedWarehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponseDTO getWarehouseByUuid(UUID uuid) {
        Warehouse warehouse = findEntityByUuid(uuid);
        return warehouseMapper.toResponseDTO(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> getAllActiveWarehouses() {
        return warehouseRepository.findAll().stream()
                .filter(Warehouse::isActive)
                .map(warehouseMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public WarehouseResponseDTO updateWarehouse(UUID uuid, WarehouseRequestDTO request) {
        Warehouse warehouse = findEntityByUuid(uuid);

        if (!warehouse.getCode().equalsIgnoreCase(request.code()) &&
                warehouseRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessRuleException("El nuevo código de almacén ya está en uso");
        }

        warehouseMapper.updateEntityFromDto(request, warehouse);
        return warehouseMapper.toResponseDTO(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public void deleteWarehouse(UUID uuid) {
        Warehouse warehouse = findEntityByUuid(uuid);
        warehouse.setActive(false);
        warehouse.setDeletedAt(ZonedDateTime.now());
        warehouseRepository.save(warehouse);
    }

    private Warehouse findEntityByUuid(UUID uuid) {
        return warehouseRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));
    }
}