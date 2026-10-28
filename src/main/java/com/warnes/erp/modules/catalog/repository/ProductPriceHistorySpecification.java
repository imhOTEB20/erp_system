package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.ProductPriceHistory;
import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductPriceHistorySpecification {

    public static Specification<ProductPriceHistory> ProductPriceInRange(
            UUID productUuid,
            ZonedDateTime from,
            ZonedDateTime to
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            var productJoin = root.join("product", JoinType.INNER);
            predicates.add(cb.equal(productJoin.get("uuid"), productUuid));

            // Si 'from' no es null, agregar condición
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            // Si 'to' no es null, agregar condición
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
