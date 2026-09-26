package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductUuidOrderByCreatedAtDesc(UUID productUuid);

    @Query(value = "SELECT sm.* FROM stock_movements as sm INNER JOIN products as p ON sm.product_id = p.id WHERE p.uuid = :uuid ORDER BY sm.created_at DESC", nativeQuery = true)
    List<StockMovement> getKardexByProductUuidBypassingSoftDeletes(@Param("uuid") UUID uuid);
}