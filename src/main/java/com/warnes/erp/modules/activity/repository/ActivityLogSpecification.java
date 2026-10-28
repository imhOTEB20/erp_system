package com.warnes.erp.modules.activity.repository;

import com.warnes.erp.modules.activity.entity.ActivityLog;
import com.warnes.erp.modules.activity.entity.ActivityType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.UUID;

public final class ActivityLogSpecification {

    private ActivityLogSpecification() {}

    public static Specification<ActivityLog> filter(
            UUID warehouseUuid,
            UUID customerUuid,
            UUID cashRegisterUuid,
            String module,
            ActivityType activityType,
            ZonedDateTime from,
            ZonedDateTime to
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (warehouseUuid != null) {
                var originJoin = root.join("warehouse", JoinType.LEFT);
                var targetJoin = root.join("targetWarehouse", JoinType.LEFT);
                predicates = cb.and(predicates, cb.or(
                        cb.equal(originJoin.get("uuid"), warehouseUuid),
                        cb.equal(targetJoin.get("uuid"), warehouseUuid)
                ));
            }

            if (customerUuid != null) {
                var customerJoin = root.join("customer", JoinType.INNER);
                predicates = cb.and(predicates, cb.equal(customerJoin.get("uuid"), customerUuid));
            }

            if (cashRegisterUuid != null) {
                var cashRegisterJoin = root.join("cashRegister", JoinType.INNER);
                predicates = cb.and(predicates, cb.equal(cashRegisterJoin.get("uuid"), cashRegisterUuid));
            }

            if (module != null && !module.isBlank()) {
                predicates = cb.and(predicates, cb.equal(cb.upper(root.get("module")), module.trim().toUpperCase()));
            }

            if (activityType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("activityType"), activityType));
            }

            if (from != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return predicates;
        };
    }
}