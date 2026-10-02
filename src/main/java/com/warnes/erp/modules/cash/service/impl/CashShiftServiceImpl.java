package com.warnes.erp.modules.cash.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.cash.dto.request.*;
import com.warnes.erp.modules.cash.dto.response.CashMovementResponseDTO;
import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;
import com.warnes.erp.modules.cash.entity.CashMovement;
import com.warnes.erp.modules.cash.entity.CashMovementType;
import com.warnes.erp.modules.cash.entity.CashRegister;
import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.cash.mapper.CashMovementMapper;
import com.warnes.erp.modules.cash.mapper.CashShiftMapper;
import com.warnes.erp.modules.cash.repository.CashMovementRepository;
import com.warnes.erp.modules.cash.repository.CashRegisterRepository;
import com.warnes.erp.modules.cash.repository.CashShiftRepository;
import com.warnes.erp.modules.cash.repository.CashShiftSpecification;
import com.warnes.erp.modules.cash.service.CashShiftService;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashShiftServiceImpl implements CashShiftService {

    private final CashShiftRepository cashShiftRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final CashMovementRepository cashMovementRepository;
    private final UserRepository userRepository;
    private final CashShiftMapper cashShiftMapper;
    private final CashMovementMapper cashMovementMapper;

    @Override
    @Transactional
    public CashShiftResponseDTO openShift(OpenShiftRequestDTO request) {
        CashRegister register = cashRegisterRepository.findByUuid(request.cashRegisterUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));

        if (cashShiftRepository.findByCashRegisterIdAndStatus(register.getId(), ShiftStatus.OPEN).isPresent()) {
            throw new BusinessRuleException("La caja ya posee un turno abierto");
        }

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
    @Transactional
    public CashShiftResponseDTO closeShift(UUID shiftUuid, CloseShiftRequestDTO request) {
        CashShift shift = cashShiftRepository.findByUuid(shiftUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessRuleException("El turno de caja ya está cerrado");
        }

        User currentUser = getCurrentAuthenticatedUser();
        BigDecimal difference = request.declaredCash().subtract(shift.getExpectedBalance());

        shift.setDeclaredBalance(request.declaredCash());
        shift.setDiscrepancy(difference);
        shift.setObservations(request.observations());
        shift.setStatus(ShiftStatus.CLOSED);
        shift.setEndedAt(ZonedDateTime.now());
        shift.setClosedByUser(currentUser);

        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            CashMovement auditMovement = new CashMovement();
            auditMovement.setCashShift(shift);
            auditMovement.setUser(currentUser);
            auditMovement.setAmount(difference.abs());
            auditMovement.setMovementType(difference.compareTo(BigDecimal.ZERO) < 0 ? CashMovementType.SHORTAGE : CashMovementType.OVERAGE);
            auditMovement.setDescription(difference.compareTo(BigDecimal.ZERO) < 0 ? "FALTANTE de caja (cierre ciego)" : "SOBRANTE de caja (cierre ciego)");
            cashMovementRepository.save(auditMovement);
        }

        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shift));
    }

    @Override
    @Transactional
    public CashShiftResponseDTO registerMovement(UUID shiftUuid, CashMovementRequestDTO request) {
        CashShift shift = getOpenShift(shiftUuid);
        User currentUser = getCurrentAuthenticatedUser();

        CashMovement movement = new CashMovement();
        movement.setCashShift(shift);
        movement.setUser(currentUser);
        movement.setMovementType(request.movementType());
        movement.setAmount(request.amount());
        movement.setDescription(request.description());

        BigDecimal currentExpected = shift.getExpectedBalance();
        if (request.movementType() == CashMovementType.INCOME || request.movementType() == CashMovementType.SALE_PAYMENT) {
            shift.setExpectedBalance(currentExpected.add(request.amount()));
        } else if (request.movementType() == CashMovementType.EXPENSE || request.movementType() == CashMovementType.REFUND) {
            shift.setExpectedBalance(currentExpected.subtract(request.amount()));
        }

        cashMovementRepository.save(movement);
        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shift));
    }

    @Override
    @Transactional
    public CashShiftResponseDTO registerSafeDrop(UUID shiftUuid, CashAdjustmentRequestDTO request) {
        CashShift shift = getOpenShift(shiftUuid);
        if (shift.getExpectedBalance().compareTo(request.amount()) < 0) {
            throw new BusinessRuleException("Fondos insuficientes para sangría");
        }

        CashMovement drop = new CashMovement();
        drop.setCashShift(shift);
        drop.setUser(getCurrentAuthenticatedUser());
        drop.setMovementType(CashMovementType.SAFE_DROP);
        drop.setAmount(request.amount());
        drop.setDescription("Sangría: " + request.description());

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
        pettyCash.setUser(getCurrentAuthenticatedUser());
        pettyCash.setMovementType(CashMovementType.PETTY_CASH_IN);
        pettyCash.setAmount(request.amount());
        pettyCash.setDescription("Ingreso sencillo: " + request.description());

        cashMovementRepository.save(pettyCash);
        shift.setExpectedBalance(shift.getExpectedBalance().add(request.amount()));

        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shift));
    }

    // --- NUEVAS IMPLEMENTACIONES SOLICITADAS ---

    @Override
    @Transactional(readOnly = true)
    public CashShiftResponseDTO getActiveShiftByRegister(UUID registerUuid) {
        CashRegister register = cashRegisterRepository.findByUuid(registerUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));
        return cashShiftRepository.findByCashRegisterIdAndStatus(register.getId(), ShiftStatus.OPEN)
                .map(cashShiftMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No hay turno abierto para esta caja"));
    }

    @Override
    @Transactional(readOnly = true)
    public CashShiftResponseDTO getShiftByUuid(UUID uuid) {
        return cashShiftRepository.findByUuid(uuid)
                .map(cashShiftMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CashShiftResponseDTO> getShifts(UUID warehouseUuid, UUID registerUuid, UUID userUuid, ShiftStatus status, ZonedDateTime from, ZonedDateTime to, Pageable pageable) {
        Specification<CashShift> spec = CashShiftSpecification.withDynamicFilters(warehouseUuid, registerUuid, userUuid, status, from, to);
        return cashShiftRepository.findAll(spec, pageable).map(cashShiftMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CashShiftResponseDTO getMyActiveShift() {
        User currentUser = getCurrentAuthenticatedUser();
        return cashShiftRepository.findByUserIdAndStatus(currentUser.getId(), ShiftStatus.OPEN)
                .map(cashShiftMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No posees ningún turno abierto."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashShiftResponseDTO> getActiveShifts(UUID warehouseUuid) {
        return cashShiftRepository.findActiveShiftsByWarehouse(ShiftStatus.OPEN, warehouseUuid)
                .stream().map(cashShiftMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CashMovementResponseDTO> getShiftMovements(UUID shiftUuid, Pageable pageable) {
        CashShift shift = cashShiftRepository.findByUuid(shiftUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        return cashMovementRepository.findByCashShiftIdOrderByCreatedAtDesc(shift.getId(), pageable)
                .map(cashMovementMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public CashShiftResponseDTO reopenShift(UUID shiftUuid) {
        CashShift shiftToReopen = cashShiftRepository.findByUuid(shiftUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        if (shiftToReopen.getStatus() == ShiftStatus.OPEN) {
            throw new BusinessRuleException("El turno ya se encuentra abierto.");
        }

        CashShift lastShift = cashShiftRepository.findFirstByCashRegisterIdOrderByOpenedAtDesc(shiftToReopen.getCashRegister().getId())
                .orElseThrow(() -> new BusinessRuleException("Error de consistencia"));

        if (!lastShift.getId().equals(shiftToReopen.getId())) {
            throw new BusinessRuleException("Solo se permite reabrir el turno más reciente de la caja.");
        }

        if (cashShiftRepository.findByCashRegisterIdAndStatus(shiftToReopen.getCashRegister().getId(), ShiftStatus.OPEN).isPresent()) {
            throw new BusinessRuleException("La caja posee un turno actualmente abierto. Ciérrelo primero.");
        }

        User currentUser = getCurrentAuthenticatedUser();

        // 1. Revertimos la auditoría usando el monto exacto de la discrepancia[cite: 1] para mantener la agregación de SUM(amount)
        if (shiftToReopen.getDiscrepancy() != null && shiftToReopen.getDiscrepancy().compareTo(BigDecimal.ZERO) != 0) {
            CashMovement adjustment = new CashMovement();
            adjustment.setCashShift(shiftToReopen);
            adjustment.setUser(currentUser);
            adjustment.setMovementType(CashMovementType.SYSTEM_ADJUSTMENT);
            adjustment.setAmount(shiftToReopen.getDiscrepancy().abs());
            adjustment.setDescription("Reapertura ADMIN. Corrección de discrepancia: " + shiftToReopen.getDiscrepancy());
            cashMovementRepository.save(adjustment);
        }

        // 2. Limpieza inmutable
        shiftToReopen.setClosedAt(null);
        shiftToReopen.setReportedBalance(null);
        shiftToReopen.setDeclaredBalance(null);
        shiftToReopen.setDiscrepancy(null);
        shiftToReopen.setEndedAt(null);
        shiftToReopen.setClosedByUser(null);
        shiftToReopen.setStatus(ShiftStatus.OPEN);

        return cashShiftMapper.toResponseDTO(cashShiftRepository.save(shiftToReopen));
    }

    private CashShift getOpenShift(UUID shiftUuid) {
        CashShift shift = cashShiftRepository.findByUuid(shiftUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessRuleException("El turno está cerrado");
        }
        return shift;
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario audit no encontrado"));
    }
}