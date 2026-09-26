package com.warnes.erp.modules.inventory.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.WarehouseUpdateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseAuditResponseDTO;
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
    public List<WarehouseResponseDTO> getAll() {
        return warehouseRepository.findAllByOrderByName().stream()
                .filter(warehouse -> {
                    return warehouse.getDeletedAt() == null;
                })
                .map(warehouseMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> getAllActiveWarehouses() {
        return warehouseRepository.findByActiveTrue().stream()
                .map(warehouseMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public WarehouseResponseDTO updateWarehouse(UUID uuid, WarehouseUpdateRequestDTO request) {
        Warehouse warehouse = findEntityByUuid(uuid);

        // Validación de unicidad solo si el cliente decidió enviar un nuevo código
        if (request.code() != null && !warehouse.getCode().equalsIgnoreCase(request.code()) &&
                warehouseRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessRuleException("El nuevo código de almacén ya está en uso");
        }

        warehouseMapper.updateEntityFromDto(request, warehouse);
        return warehouseMapper.toResponseDTO(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public Boolean toggleAvailability(UUID uuid) {
        Warehouse warehouse = findEntityByUuid(uuid);
        warehouse.setActive(!warehouse.isActive());

        warehouseRepository.save(warehouse);
        return warehouse.isActive();
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

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseAuditResponseDTO> getDeletedWarehouses() {
        return warehouseRepository.findAllDeletedWarehouses().stream()
                .map(warehouseMapper::toAuditResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public WarehouseAuditResponseDTO restoreWarehouse(UUID uuid) {
        // 1. Buscamos ignorando el filtro de soft-delete
        Warehouse warehouse = warehouseRepository.findAnyWarehouseByUuidIncludingDeleted(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado en los registros históricos"));

        // 2. Regla de Negocio: No restaurar algo que ya está activo
        if (warehouse.getDeletedAt() == null) {
            throw new BusinessRuleException("El almacén ya se encuentra activo en el sistema");
        }

        // 3. Ejecutamos el UPDATE nativo para evadir el @SQLRestriction
        warehouseRepository.restoreWarehouseByUuid(uuid);

        // 4. Actualizamos la entidad en memoria para devolver el DTO limpio al frontend
        warehouse.setDeletedAt(null);
        warehouse.setActive(true);
        warehouse.setUpdatedAt(ZonedDateTime.now());

        return warehouseMapper.toAuditResponseDTO(warehouse);
    }
}