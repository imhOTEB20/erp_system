package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByUuid(UUID uuid);
    boolean existsByCodeIgnoreCase(String code);
    List<Warehouse> findAllByOrderByName();
    List<Warehouse> findByActiveTrue();

    // 1. Recuperar solo los eliminados (Papelera)
    @Query(value = "SELECT * FROM warehouses WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<Warehouse> findAllDeletedWarehouses();

    // 2. Recuperar sin importar el estado (Auditoría/Validación previa)
    @Query(value = "SELECT * FROM warehouses WHERE uuid = :uuid", nativeQuery = true)
    Optional<Warehouse> findAnyWarehouseByUuidIncludingDeleted(@Param("uuid") UUID uuid);

    // 3. Forzar el UPDATE saltándose el @SQLRestriction de Hibernate
    @Modifying
    @Query(value = "UPDATE warehouses SET deleted_at = NULL, active = true, updated_at = CURRENT_TIMESTAMP WHERE uuid = :uuid", nativeQuery = true)
    void restoreWarehouseByUuid(@Param("uuid") UUID uuid);
}