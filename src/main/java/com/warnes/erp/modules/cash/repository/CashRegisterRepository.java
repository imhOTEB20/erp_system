package com.warnes.erp.modules.cash.repository;

import com.warnes.erp.modules.cash.entity.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    Optional<CashRegister> findByUuid(UUID uuid);
    boolean existsByNameIgnoreCase(String name);
}