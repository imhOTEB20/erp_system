package com.warnes.erp.modules.sales.repository;

import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SaleSpecification {
    public static Specification<Sale> withDynamicFilters(SaleStatus status, UUID customerUuid, ZonedDateTime from, ZonedDateTime to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (customerUuid != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("uuid"), customerUuid));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Sale> completedSalesInDateRange(
            UUID warehouseUuid,
            ZonedDateTime from,
            ZonedDateTime to
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre filtrar por estado COMPLETED
            predicates.add(cb.equal(root.get("status"), SaleStatus.COMPLETED));

            if (warehouseUuid != null) {
                var cashShiftJoin = root.join("cashShift", JoinType.INNER);
                var cashRegisterJoin = cashShiftJoin.join("cashRegister", JoinType.INNER);
                var warehouseJoin = cashRegisterJoin.join("warehouse", JoinType.INNER);
                predicates.add(cb.equal(warehouseJoin.get("uuid"), warehouseUuid));
            }

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