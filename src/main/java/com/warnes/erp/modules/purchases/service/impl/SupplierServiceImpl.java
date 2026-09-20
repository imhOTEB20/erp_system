package com.warnes.erp.modules.purchases.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.purchases.dto.request.SupplierRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.SupplierResponseDTO;
import com.warnes.erp.modules.purchases.entity.Supplier;
import com.warnes.erp.modules.purchases.mapper.SupplierMapper;
import com.warnes.erp.modules.purchases.repository.SupplierRepository;
import com.warnes.erp.modules.purchases.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierResponseDTO createSupplier(SupplierRequestDTO request) {
        if (supplierRepository.existsByTaxId(request.taxId())) {
            throw new BusinessRuleException("Ya existe un proveedor registrado con ese identificador fiscal");
        }
        Supplier supplier = supplierMapper.toEntity(request);
        return supplierMapper.toResponseDTO(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDTO getSupplierByUuid(UUID uuid) {
        return supplierMapper.toResponseDTO(findEntityByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> getAllActiveSuppliers() {
        return supplierRepository.findAll().stream()
                .filter(Supplier::isActive)
                .map(supplierMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public SupplierResponseDTO updateSupplier(UUID uuid, SupplierRequestDTO request) {
        Supplier supplier = findEntityByUuid(uuid);

        if (!supplier.getTaxId().equals(request.taxId()) && supplierRepository.existsByTaxId(request.taxId())) {
            throw new BusinessRuleException("El nuevo identificador fiscal ya está en uso");
        }

        supplierMapper.updateEntityFromDto(request, supplier);
        return supplierMapper.toResponseDTO(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void deleteSupplier(UUID uuid) {
        Supplier supplier = findEntityByUuid(uuid);
        supplier.setActive(false);
        supplier.setDeletedAt(ZonedDateTime.now());
        supplierRepository.save(supplier);
    }

    private Supplier findEntityByUuid(UUID uuid) {
        return supplierRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
    }
}