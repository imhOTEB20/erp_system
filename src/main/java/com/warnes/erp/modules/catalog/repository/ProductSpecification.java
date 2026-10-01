package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.entity.ProductCrossReference;
import com.warnes.erp.modules.inventory.entity.Stock;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductSpecification {

    public static Specification<Product> withDynamicFilters(
            UUID categoryUuid,
            UUID brandUuid,
            Boolean isKit,
            Boolean isService,
            Boolean isActive,
            Boolean includeDeleted
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("brand", JoinType.LEFT);
                root.fetch("taxType", JoinType.LEFT);
            }

            if (categoryUuid != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("uuid"), categoryUuid));
            }

            if (brandUuid != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand").get("uuid"), brandUuid));
            }

            if (isKit != null) {
                predicates.add(criteriaBuilder.equal(root.get("kit"), isKit));
            }

            if (isService != null) {
                predicates.add(criteriaBuilder.equal(root.get("service"), isService));
            }

            // Filtro por active
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), isActive));
            }

            // Filtro por deletedAt
            if (includeDeleted == null || Boolean.FALSE.equals(includeDeleted)) {
                predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> matchesSkuOrEquivalenceOrName(String query, Boolean isKit, Boolean isService, Boolean isActive, Boolean includeDeleted) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Product, ProductCrossReference> cr = root.join("crossReferences", JoinType.LEFT);
            Predicate bySku = cb.like(cb.upper(root.get("sku")), "%" + query.toUpperCase() + "%");
            Predicate byEquivalence = cb.like(cb.upper(cr.get("externalSku")), "%" + query.toUpperCase() + "%");
            Predicate byName = cb.like(cb.upper(root.get("name")), "%" + query.toUpperCase() + "%");

            Predicate filterQuery = cb.or(bySku, byEquivalence, byName);
            predicates.add(filterQuery);

            if (isKit != null) {
                predicates.add(cb.equal(root.get("kit"), isKit));
            }

            if (isService != null) {
                predicates.add(cb.equal(root.get("service"), isService));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("active"), isActive));
            }

            // Filtro por deletedAt
            if (includeDeleted == null || Boolean.FALSE.equals(includeDeleted)) {
                predicates.add(cb.isNull(root.get("deletedAt")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> withoutStockInWarehouse(UUID warehouseUuid) {
        return (root, cq, cb) -> {
            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<Stock> stock = subquery.from(Stock.class);

            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(stock.get("product"), root),
                            cb.equal(stock.get("warehouse").get("uuid"), warehouseUuid)
                    );

            return cb.not(cb.exists(subquery));
        };
    }

    public static Specification<Product> withIncludeDeleted(Boolean includeDeleted) {
        return (root, cq, cb) -> {
            if (Boolean.TRUE.equals(includeDeleted)) {
                // No filtro nada, incluyo todos
                return cb.conjunction();
            } else {
                // Si es false o null, excluyo los eliminados
                return cb.isNull(root.get("deletedAt"));
            }
        };
    }

    public static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Product> deleted() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<Product> notDisabled() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> hasUuid(UUID uuid) {
        return (root, query, cb) -> cb.equal(root.get("uuid"), uuid);
    }
}