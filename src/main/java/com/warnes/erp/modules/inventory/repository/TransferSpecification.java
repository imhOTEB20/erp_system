package com.warnes.erp.modules.inventory.repository;

import com.warnes.erp.modules.inventory.entity.Transfer;
import com.warnes.erp.modules.inventory.entity.TransferStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class TransferSpecification {

    private TransferSpecification() {}

    public static Specification<Transfer> hasStatusAndWarehouse(TransferStatus status, UUID warehouseUuid) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            if (warehouseUuid != null) {
                var originJoin = root.join("originWarehouse", JoinType.LEFT);
                var targetJoin = root.join("targetWarehouse", JoinType.LEFT);
                predicates = cb.and(predicates, cb.or(
                        cb.equal(originJoin.get("uuid"), warehouseUuid),
                        cb.equal(targetJoin.get("uuid"), warehouseUuid)
                ));
            }

            return predicates;
        };
    }

    public static Specification<Transfer> hasTrackingCode(String trackingCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("trackingCode"), trackingCode);
    }
}