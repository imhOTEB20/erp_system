package com.warnes.erp.modules.inventory.mapper;

import com.warnes.erp.modules.inventory.dto.request.WarehouseRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.WarehouseUpdateRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseAuditResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.WarehouseResponseDTO;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarehouseMapper {

    Warehouse toEntity(WarehouseRequestDTO request);

    WarehouseResponseDTO toResponseDTO(Warehouse entity);

    // NUEVO: Mapeo hacia el DTO de auditoría (MapStruct mapeará las fechas automáticamente desde AuditableEntity)
    WarehouseAuditResponseDTO toAuditResponseDTO(Warehouse entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(WarehouseUpdateRequestDTO request, @MappingTarget Warehouse entity);
}