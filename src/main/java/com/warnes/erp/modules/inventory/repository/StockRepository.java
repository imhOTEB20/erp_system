package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    // Bloqueo pesimista: Evita que otra transacción modifique el stock simultáneamente
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId")
    Optional<Stock> findByProductAndWarehouseForUpdate(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}