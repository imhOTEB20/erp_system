package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long>, JpaSpecificationExecutor<ProductPriceHistory> {
}
