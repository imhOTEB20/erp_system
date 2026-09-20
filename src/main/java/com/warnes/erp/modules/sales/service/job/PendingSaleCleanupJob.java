package com.warnes.erp.modules.sales.service.job;

import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import com.warnes.erp.modules.sales.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingSaleCleanupJob {

    private final SaleRepository saleRepository;

    // Ejecuta cada 30 minutos
    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void cleanupAbandonedSales() {
        ZonedDateTime threshold = ZonedDateTime.now().minusHours(2); // Caducidad de 2 horas

        List<Sale> abandonedSales = saleRepository.findByStatusAndCreatedAtBefore(SaleStatus.PENDING_PAYMENT, threshold);

        if (abandonedSales.isEmpty()) return;

        log.info("Iniciando limpieza de {} ventas abandonadas...", abandonedSales.size());

        for (Sale sale : abandonedSales) {
            // Revertir pagos parciales si existieran (ej. devolver saldo a favor)
            // Lógica omitida para brevedad: iterar payments y si es STORE_CREDIT, sumar al customer

            sale.setStatus(SaleStatus.CANCELLED);
            saleRepository.save(sale);
        }

        log.info("Limpieza de ventas finalizada.");
    }
}