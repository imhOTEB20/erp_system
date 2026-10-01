package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TaxTypeRepository extends JpaRepository<TaxType, Long>, JpaSpecificationExecutor<TaxType> {
    Optional<TaxType> findByUuid(UUID uuid);
}