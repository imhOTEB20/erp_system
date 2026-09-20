package com.warnes.erp.modules.inventory.mapper;

import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseResponseDTO;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarehouseMapper {

    Warehouse toEntity(WarehouseRequestDTO request);

    WarehouseResponseDTO toResponseDTO(Warehouse entity);

    void updateEntityFromDto(WarehouseRequestDTO request, @MappingTarget Warehouse entity);
}