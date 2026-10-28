package com.warnes.erp.modules.activity.mapper;

import com.warnes.erp.modules.activity.dto.request.ActivityLogRequestDTO;
import com.warnes.erp.modules.activity.dto.response.ActivityLogResponseDTO;
import com.warnes.erp.modules.activity.entity.ActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(target = "warehouseUuid", source = "warehouse.uuid")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "targetWarehouseUuid", source = "targetWarehouse.uuid")
    @Mapping(target = "targetWarehouseName", source = "targetWarehouse.name")
    @Mapping(target = "customerUuid", source = "customer.uuid")
    @Mapping(target = "customerName", source = "customer.businessName")
    @Mapping(target = "cashRegisterUuid", source = "cashRegister.uuid")
    @Mapping(target = "cashRegisterName", source = "cashRegister.name")
    ActivityLogResponseDTO toResponseDTO(ActivityLog entity);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "targetWarehouse", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "cashRegister", ignore = true)
    @Mapping(target = "performedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ActivityLog toEntity(ActivityLogRequestDTO requestDTO);
}