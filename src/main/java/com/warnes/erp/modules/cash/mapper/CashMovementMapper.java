package com.warnes.erp.modules.cash.mapper;

import com.warnes.erp.modules.cash.dto.response.CashMovementResponseDTO;
import com.warnes.erp.modules.cash.entity.CashMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CashMovementMapper {
    @Mapping(target = "userName", source = "user.fullName")
    CashMovementResponseDTO toResponseDTO(CashMovement entity);
}