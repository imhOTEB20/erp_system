package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByUuid(UUID uuid);
    boolean existsByTrackingCodeIgnoreCase(String trackingCode);
}