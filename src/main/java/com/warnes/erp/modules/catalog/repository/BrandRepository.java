package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByUuid(UUID uuid);
}