package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByUuid(UUID uuid);
    boolean existsByCodeIgnoreCase(String code);
}