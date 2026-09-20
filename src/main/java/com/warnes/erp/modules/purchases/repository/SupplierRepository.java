package com.warnes.erp.modules.purchases.repository;

import com.warnes.erp.modules.purchases.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByUuid(UUID uuid);
    boolean existsByTaxId(String taxId);
}