package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByUuid(UUID uuid);
    Optional<Transfer> findByTrackingCode(String trackingCode);
    List<Transfer> findAllByOrderByCreatedAtDesc();
    boolean existsByTrackingCodeIgnoreCase(String trackingCode);
}