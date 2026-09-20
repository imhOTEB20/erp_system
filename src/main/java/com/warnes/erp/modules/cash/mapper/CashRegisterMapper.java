package com.warnes.erp.modules.cash.mapper;

import com.warnes.erp.modules.cash.dto.request.CashRegisterRequestDTO;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.entity.CashRegister;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CashRegisterMapper {
    CashRegister toEntity(CashRegisterRequestDTO request);
    CashRegisterResponseDTO toResponseDTO(CashRegister entity);
    void updateEntityFromDto(CashRegisterRequestDTO request, @MappingTarget CashRegister entity);
}