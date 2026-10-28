package com.warnes.erp.modules.activity.repository;

import com.warnes.erp.modules.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, JpaSpecificationExecutor<ActivityLog> {

    @EntityGraph(attributePaths = {"warehouse", "targetWarehouse", "customer", "cashRegister"})
    Optional<ActivityLog> findByUuid(UUID uuid);

    @Override
    @EntityGraph(attributePaths = {"warehouse", "targetWarehouse", "customer", "cashRegister"})
    Page<ActivityLog> findAll(Specification<ActivityLog> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"warehouse", "targetWarehouse", "customer", "cashRegister"})
    List<ActivityLog> findTop20ByOrderByCreatedAtDesc();
}