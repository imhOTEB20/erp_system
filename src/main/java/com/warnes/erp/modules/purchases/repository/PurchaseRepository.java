package com.warnes.erp.modules.purchases.repository;

import com.warnes.erp.modules.purchases.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByUuid(UUID uuid);
    boolean existsBySupplierIdAndInvoiceNumberIgnoreCase(Long supplierId, String invoiceNumber);
}