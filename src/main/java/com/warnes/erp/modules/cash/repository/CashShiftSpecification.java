package com.warnes.erp.modules.cash.repository;

import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CashShiftSpecification {
    public static Specification<CashShift> withDynamicFilters(
            UUID warehouseUuid,
            UUID registerUuid,
            UUID userUuid,
            ShiftStatus status,
            ZonedDateTime from,
            ZonedDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (warehouseUuid != null) {
                predicates.add(criteriaBuilder.equal(root.get("cashRegister").get("warehouse").get("uuid"), warehouseUuid));
            }
            if (registerUuid != null) {
                predicates.add(criteriaBuilder.equal(root.get("cashRegister").get("uuid"), registerUuid));
            }
            if (userUuid != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("uuid"), userUuid));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("openedAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("openedAt"), to));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}