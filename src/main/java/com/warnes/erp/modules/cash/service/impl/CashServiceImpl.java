package com.warnes.erp.modules.cash.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;
import com.warnes.erp.modules.cash.entity.CashMovement;
import com.warnes.erp.modules.cash.entity.CashMovementType;
import com.warnes.erp.modules.cash.entity.CashRegister;
import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.cash.mapper.CashRegisterMapper;
import com.warnes.erp.modules.cash.mapper.CashShiftMapper;
import com.warnes.erp.modules.cash.repository.CashMovementRepository;
import com.warnes.erp.modules.cash.repository.CashRegisterRepository;
import com.warnes.erp.modules.cash.repository.CashShiftRepository;
import com.warnes.erp.modules.cash.service.CashService;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {

    private final CashRegisterRepository cashRegisterRepository;
    private final CashShiftRepository cashShiftRepository;
    private final CashMovementRepository cashMovementRepository;
    private final UserRepository userRepository;
    private final CashRegisterMapper cashRegisterMapper;
    private final CashShiftMapper cashShiftMapper;

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
    @Transactional
    public CashShiftResponseDTO openShift(OpenShiftRequestDTO request) {
        CashRegister register = cashRegisterRepository.findByUuid(request.cashRegisterUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));

        cashShiftRepository.findByCashRegisterIdAndStatus(register.getId(), ShiftStatus.OPEN)
                .ifPresent(s -> {
                    throw new BusinessRuleException("La caja ya posee un turno abierto actualmente");
                });

        User currentUser = getCurrentAuthenticatedUser();

        CashShift shift = new CashShift();
        shift.setCashRegister(register);
        shift.setUser(currentUser);
        shift.setOpeningBalance(request.openingBalance());
        shift.setExpectedBalance(request.openingBalance());
        shift.setStatus(ShiftStatus.OPEN);

        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shift));
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
    public CashShiftResponseDTO closeShift(UUID shiftUuid, CloseShiftRequestDTO request) {
        CashShift shift = cashShiftRepository.findByUuid(shiftUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Turno de caja no encontrado"));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessRuleException("El turno de caja ya se encuentra cerrado");
        }

        // 1. Auditoría del Cierre Ciego
        BigDecimal expected = shift.getExpectedBalance();
        BigDecimal declared = request.declaredCash();
        BigDecimal difference = declared.subtract(expected); // Si es negativo, falta plata. Si es positivo, sobra.

        shift.setDeclaredBalance(declared);
        shift.setDiscrepancy(difference);
        shift.setObservations(request.observations());
        shift.setStatus(ShiftStatus.CLOSED);
        shift.setEndedAt(ZonedDateTime.now());

        // 2. Registro Automático del Descuadre
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            CashMovement auditMovement = new CashMovement();
            auditMovement.setCashShift(shift);
            auditMovement.setAmount(difference.abs()); // El movimiento siempre se registra en valor absoluto

            if (difference.compareTo(BigDecimal.ZERO) < 0) {
                auditMovement.setMovementType(CashMovementType.SHORTAGE);
                auditMovement.setDescription("FALTANTE de caja detectado en el cierre ciego automático.");
            } else {
                auditMovement.setMovementType(CashMovementType.OVERAGE);
                auditMovement.setDescription("SOBRANTE de caja detectado en el cierre ciego automático.");
            }
            cashMovementRepository.save(auditMovement);
        }

        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shift));
    }

    @Override
    @Transactional
    public CashShiftResponseDTO registerMovement(UUID shiftUuid, CashMovementRequestDTO request) {
        CashShift shift = cashShiftRepository.findByUuid(shiftUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Turno de caja no encontrado"));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessRuleException("No se pueden registrar movimientos en un turno cerrado");
        }

        CashMovement movement = new CashMovement();
        movement.setCashShift(shift);
        movement.setMovementType(request.movementType());
        movement.setAmount(request.amount());
        movement.setDescription(request.description());

        // Actualizamos el balance esperado del turno de forma atómica
        BigDecimal currentExpected = shift.getExpectedBalance();
        if (request.movementType() == CashMovementType.INCOME || request.movementType() == CashMovementType.SALE_PAYMENT) {
            shift.setExpectedBalance(currentExpected.add(request.amount()));
        } else if (request.movementType() == CashMovementType.EXPENSE || request.movementType() == CashMovementType.REFUND) {
            shift.setExpectedBalance(currentExpected.subtract(request.amount()));
        }

        cashMovementRepository.save(movement);
        cashShiftRepository.save(shift);

        return cashShiftMapper.toResponseDTO(shift);
    }

    @Override
    @Transactional(readOnly = true)
    public CashShiftResponseDTO getActiveShiftByRegister(UUID registerUuid) {
        CashRegister register = cashRegisterRepository.findByUuid(registerUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));

        CashShift shift = cashShiftRepository.findByCashRegisterIdAndStatus(register.getId(), ShiftStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No hay ningún turno abierto para esta caja"));

        return cashShiftMapper.toResponseDTO(shift);
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de auditoría no encontrado"));
    }

    @Override
    @Transactional
    public CashShiftResponseDTO registerSafeDrop(UUID shiftUuid, CashAdjustmentRequestDTO request) {
        CashShift shift = getOpenShift(shiftUuid);

        if (shift.getExpectedBalance().compareTo(request.amount()) < 0) {
            throw new BusinessRuleException("No hay suficiente efectivo en caja para realizar la sangría");
        }

        CashMovement drop = new CashMovement();
        drop.setCashShift(shift);
        drop.setMovementType(CashMovementType.SAFE_DROP);
        drop.setAmount(request.amount());
        drop.setDescription("Sangría de seguridad: " + request.description());
        cashMovementRepository.save(drop);

        shift.setExpectedBalance(shift.getExpectedBalance().subtract(request.amount()));
        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shift));
    }

    @Override
    @Transactional
    public CashShiftResponseDTO registerPettyCashIn(UUID shiftUuid, CashAdjustmentRequestDTO request) {
        CashShift shift = getOpenShift(shiftUuid);

        CashMovement pettyCash = new CashMovement();
        pettyCash.setCashShift(shift);
        pettyCash.setMovementType(CashMovementType.PETTY_CASH_IN);
        pettyCash.setAmount(request.amount());
        pettyCash.setDescription("Ingreso de sencillo: " + request.description());
        cashMovementRepository.save(pettyCash);

        shift.setExpectedBalance(shift.getExpectedBalance().add(request.amount()));
        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shift));
    }

    private CashShift getOpenShift(UUID shiftUuid) {
        CashShift shift = cashShiftRepository.findByUuid(shiftUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessRuleException("El turno de caja está cerrado");
        }
        return shift;
    }
}