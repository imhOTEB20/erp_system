package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductUuidOrderByCreatedAtDesc(UUID productUuid);
}