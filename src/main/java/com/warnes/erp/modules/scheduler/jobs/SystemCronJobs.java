package com.warnes.erp.modules.scheduler.jobs;

import com.warnes.erp.modules.scheduler.service.SystemMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemCronJobs {

    private final SystemMaintenanceService maintenanceService;

    /**
     * Limpieza de Ventas Abandonadas.
     * Se ejecuta cada hora, en el minuto 0. (Ej: 14:00, 15:00, 16:00...)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupAbandonedSalesJob() {
        log.debug("Ejecutando Job de limpieza de ventas abandonadas...");
        maintenanceService.cancelAbandonedSales();
    }

    /**
     * Auditoría y Cierre Forzoso de Cajas Registradoras.
     * Se ejecuta todos los días de madrugada a las 03:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void forceCloseForgottenShiftsJob() {
        log.info("Ejecutando Job de auditoría nocturna de cajas...");
        maintenanceService.autoCloseForgottenCashShifts();
    }
}