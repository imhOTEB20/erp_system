package com.warnes.erp.modules.scheduler.service.impl;

import com.warnes.erp.modules.cash.entity.CashMovement;
import com.warnes.erp.modules.cash.entity.CashMovementType;
import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.cash.repository.CashMovementRepository;
import com.warnes.erp.modules.cash.repository.CashShiftRepository;
import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import com.warnes.erp.modules.sales.repository.SaleRepository;
import com.warnes.erp.modules.scheduler.service.SystemMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMaintenanceServiceImpl implements SystemMaintenanceService {

    private final SaleRepository saleRepository;
    private final CashShiftRepository cashShiftRepository;
    private final CashMovementRepository cashMovementRepository;

    @Override
    @Transactional
    public void cancelAbandonedSales() {
        // Tolerancia: Ventas pendientes creadas hace más de 24 horas
        ZonedDateTime threshold = ZonedDateTime.now().minusHours(24);
        List<Sale> abandonedSales = saleRepository.findByStatusAndCreatedAtBefore(SaleStatus.PENDING_PAYMENT, threshold);

        if (!abandonedSales.isEmpty()) {
            log.info("CronJob: Encontradas {} ventas abandonadas. Procediendo a cancelar...", abandonedSales.size());
            for (Sale sale : abandonedSales) {
                sale.setStatus(SaleStatus.CANCELLED);
                // No devolvemos stock porque en nuestra arquitectura, el stock se deduce al pagar, no al crear la venta.
            }
            saleRepository.saveAll(abandonedSales);
            log.info("CronJob: Ventas abandonadas canceladas exitosamente.");
        }
    }

    @Override
    @Transactional
    public void autoCloseForgottenCashShifts() {
        // Tolerancia: Turnos que llevan abiertos más de 16 horas (un empleado olvidó cerrar al irse)
        ZonedDateTime threshold = ZonedDateTime.now().minusHours(16);
        List<CashShift> forgottenShifts = cashShiftRepository.findByStatusAndOpenedAtBefore(ShiftStatus.OPEN, threshold);

        if (!forgottenShifts.isEmpty()) {
            log.warn("CronJob: ALERTA - Encontrados {} turnos de caja abandonados. Forzando cierre ciego...", forgottenShifts.size());

            for (CashShift shift : forgottenShifts) {
                BigDecimal expected = shift.getExpectedBalance();
                // Como el sistema fuerza el cierre, se asume que el cajero no declaró nada ($0.00 en gaveta física)
                BigDecimal declared = BigDecimal.ZERO;
                BigDecimal difference = declared.subtract(expected); // Esto generará un FALTANTE masivo

                shift.setDeclaredBalance(declared);
                shift.setDiscrepancy(difference);
                shift.setObservations("CIERRE AUTOMÁTICO POR SISTEMA (CRON) - El operador omitió cerrar la caja al finalizar la jornada.");
                shift.setStatus(ShiftStatus.CLOSED);
                shift.setEndedAt(ZonedDateTime.now());

                if (difference.compareTo(BigDecimal.ZERO) < 0) {
                    CashMovement auditMovement = new CashMovement();
                    auditMovement.setCashShift(shift);
                    auditMovement.setAmount(difference.abs());
                    auditMovement.setMovementType(CashMovementType.SHORTAGE);
                    auditMovement.setDescription("FALTANTE SEVERO - Cierre automático por sistema. Dinero no declarado.");
                    cashMovementRepository.save(auditMovement);
                }
            }
            cashShiftRepository.saveAll(forgottenShifts);
            log.info("CronJob: Cierre forzoso de cajas finalizado.");
        }
    }
}