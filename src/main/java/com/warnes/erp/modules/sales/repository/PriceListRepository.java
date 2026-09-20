package com.warnes.erp.modules.sales.repository;

import com.warnes.erp.modules.sales.entity.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    Optional<PriceList> findByUuid(UUID uuid);
    boolean existsByNameIgnoreCase(String name);
}