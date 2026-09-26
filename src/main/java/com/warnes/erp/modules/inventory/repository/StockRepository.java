package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, Long> {

    // Bloqueo pesimista: Evita que otra transacción modifique el stock simultáneamente
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId")
    Optional<Stock> findByProductAndWarehouseForUpdate(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    // Traemos el stock y hacemos JOIN FETCH del producto para mapear el DTO de forma óptima
    @Query("SELECT s FROM Stock s JOIN FETCH s.product WHERE s.warehouse.uuid = :warehouseUuid")
    List<Stock> findAllByWarehouseUuid(@Param("warehouseUuid") UUID warehouseUuid);
}