package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {
    Optional<Transfer> findByUuid(UUID uuid);

}