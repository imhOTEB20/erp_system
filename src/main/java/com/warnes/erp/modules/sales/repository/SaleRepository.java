package com.warnes.erp.modules.sales.repository;

import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByUuid(UUID uuid);
    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);
    // Consulta para caducidad de ventas (Time-To-Live)
    List<Sale> findByStatusAndCreatedAtBefore(SaleStatus status, ZonedDateTime time);
}