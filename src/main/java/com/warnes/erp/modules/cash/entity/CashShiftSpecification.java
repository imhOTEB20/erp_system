package com.warnes.erp.modules.cash.entity;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CashShiftSpecification {
    public static Specification<CashShift> byStatusAndWarehouse(ShiftStatus status, UUID warehouseUuid) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (warehouseUuid != null) {
                var registerJoin = root.join("cashRegister", jakarta.persistence.criteria.JoinType.INNER);
                var warehouseJoin = registerJoin.join("warehouse", jakarta.persistence.criteria.JoinType.INNER);
                predicates = cb.and(predicates, cb.equal(warehouseJoin.get("uuid"), warehouseUuid));
            }
            return predicates;
        };
    }
}
