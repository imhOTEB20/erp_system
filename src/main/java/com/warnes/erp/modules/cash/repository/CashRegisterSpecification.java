package com.warnes.erp.modules.cash.repository;

import com.warnes.erp.modules.cash.entity.CashRegister;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CashRegisterSpecification {

    public static Specification<CashRegister> withDynamicFilters(
            UUID warehouseUuid,
            Boolean isActive,
            Boolean includeDeleted
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("warehouse", JoinType.LEFT);
            }

            if (warehouseUuid != null) {
                predicates.add(criteriaBuilder.equal(root.get("warehouse").get("uuid"), warehouseUuid));
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

    public static Specification<CashRegister> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<CashRegister> deleted() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<CashRegister> notDisabled() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<CashRegister> hasUuid(UUID uuid) {
        return (root, query, cb) -> cb.equal(root.get("uuid"), uuid);
    }
}
