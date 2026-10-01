package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findByUuid(UUID uuid);
    boolean existsByNameIgnoreCase(String name);
}