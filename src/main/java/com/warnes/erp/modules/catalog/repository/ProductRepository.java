package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByUuid(UUID uuid);
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsByUuid(UUID uuid);

    /**
     * Motor de búsqueda de mostrador: Busca el código exacto, ya sea
     * nuestro SKU principal o algún código de equivalencia cargado.
     * Usamos LEFT JOIN FETCH para evitar el problema de N+1 consultas.
     */
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.crossReferences cr " +
            "LEFT JOIN FETCH p.brand " +
            "WHERE UPPER(p.sku) = UPPER(:query) " +
            "OR UPPER(cr.externalSku) = UPPER(:query)")
    List<Product> searchBySkuOrEquivalence(@Param("query") String query);

    Optional<Product> findBySkuIgnoreCase(String sku);
}