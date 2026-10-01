package com.warnes.erp.modules.catalog.repository;

import com.warnes.erp.modules.catalog.entity.TaxType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaxTypeSpecification {

    public static Specification<TaxType> withDynamicFilters(
            Boolean isActive,
            Boolean includeDeleted
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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

    public static Specification<TaxType> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<TaxType> deleted() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<TaxType> notDisabled() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<TaxType> hasUuid(UUID uuid) {
        return (root, query, cb) -> cb.equal(root.get("uuid"), uuid);
    }
}