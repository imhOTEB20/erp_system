package com.warnes.erp.modules.cash.mapper;

import com.warnes.erp.modules.cash.dto.response.CashShiftResponseDTO;
import com.warnes.erp.modules.cash.entity.CashShift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CashShiftMapper {

    @Mapping(target = "cashRegisterUuid", source = "cashRegister.uuid")
    @Mapping(target = "cashRegisterName", source = "cashRegister.name")
    @Mapping(target = "userUuid", source = "user.uuid")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    CashShiftResponseDTO toResponseDTO(CashShift entity);
}