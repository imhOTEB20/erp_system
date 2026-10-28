package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Stock;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public final class StockSpecification {

    private StockSpecification() {}

    /**
     * Filtra productos activos cuyo stock actual sea menor o igual al minStock (quantity <= minStock).
     * Incluye FETCH JOIN automático cuando la consulta no es un COUNT.
     */
    public static Specification<Stock> belowOrEqualMinStock(UUID warehouseUuid) {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("product", JoinType.INNER);
                root.fetch("warehouse", JoinType.INNER);
            }

            var productJoin = root.join("product", JoinType.INNER);
            var warehouseJoin = root.join("warehouse", JoinType.INNER);

            var predicates = cb.and(
                    cb.isTrue(productJoin.get("active")),
                    cb.lessThanOrEqualTo(root.get("quantity"), root.get("minStock"))
            );

            if (warehouseUuid != null) {
                predicates = cb.and(predicates, cb.equal(warehouseJoin.get("uuid"), warehouseUuid));
            }

            return predicates;
        };
    }

    /**
     * Filtra productos activos con stock exactamente en 0 (quantity = 0) en una sucursal/depósito.
     */
    public static Specification<Stock> zeroStock(UUID warehouseUuid) {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("product", JoinType.INNER);
                root.fetch("warehouse", JoinType.INNER);
            }

            var productJoin = root.join("product", JoinType.INNER);
            var warehouseJoin = root.join("warehouse", JoinType.INNER);

            var predicates = cb.and(
                    cb.isTrue(productJoin.get("active")),
                    cb.equal(root.get("quantity"), BigDecimal.ZERO)
            );

            if (warehouseUuid != null) {
                predicates = cb.and(predicates, cb.equal(warehouseJoin.get("uuid"), warehouseUuid));
            }

            return predicates;
        };
    }

    public static Specification<Stock> hasProductUuid(UUID productUuid) {
        return (root, query, cb) -> {
            var productJoin = root.join("product", JoinType.INNER);

            return cb.equal(productJoin.get("uuid"), productUuid);
        };
    }

    public static Specification<Stock> hasWarehouseUuid(UUID warehouseUuid) {
        return (root, query, cb) -> {
            var warehouseJoin = root.join("warehouse", JoinType.INNER);

            return cb.equal(warehouseJoin.get("uuid"), warehouseUuid);
        };
    }
}