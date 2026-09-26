package com.warnes.erp.modules.cash.mapper;

import com.warnes.erp.modules.cash.dto.request.CashRegisterRequestDTO;
import com.warnes.erp.modules.cash.dto.response.CashRegisterResponseDTO;
import com.warnes.erp.modules.cash.entity.CashRegister;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CashRegisterMapper {

    // Ignoramos warehouseUuid en la creación, lo asociaremos en el Service
    @Mapping(target = "warehouse", ignore = true)
    CashRegister toEntity(CashRegisterRequestDTO request);

    // Mapeamos el UUID de la sucursal hacia la respuesta
    @Mapping(source = "warehouse.uuid", target = "warehouseUuid")
    CashRegisterResponseDTO toResponseDTO(CashRegister entity);

    @Mapping(target = "warehouse", ignore = true)
    void updateEntityFromDto(CashRegisterRequestDTO request, @MappingTarget CashRegister entity);
}