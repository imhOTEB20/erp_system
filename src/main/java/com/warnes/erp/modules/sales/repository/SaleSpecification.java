package com.warnes.erp.modules.sales.repository;

import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.entity.SaleStatus;
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
}