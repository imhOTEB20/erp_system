package com.warnes.erp.modules.cash.repository;

import com.warnes.erp.modules.cash.entity.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterRepository extends JpaRepository<CashRegister, Long>, JpaSpecificationExecutor<CashRegister> {
    Optional<CashRegister> findByUuid(UUID uuid);

    // Valida que no haya dos cajas con el mismo nombre en la MISMA sucursal
    boolean existsByWarehouse_IdAndNameIgnoreCase(Long warehouseId, String name);

    // Busca todas las cajas de un almacén usando el UUID público
    List<CashRegister> findByWarehouse_Uuid(UUID warehouseUuid);
}