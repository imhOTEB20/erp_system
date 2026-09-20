package com.warnes.erp.modules.inventory.mapper;

import com.warnes.erp.modules.inventory.dto.response.TransferItemResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.TransferResponseDTO;
import com.warnes.erp.modules.inventory.entity.Transfer;
import com.warnes.erp.modules.inventory.entity.TransferItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransferMapper {

    @Mapping(target = "originWarehouseUuid", source = "originWarehouse.uuid")
    @Mapping(target = "originWarehouseName", source = "originWarehouse.name")
    @Mapping(target = "targetWarehouseUuid", source = "targetWarehouse.uuid")
    @Mapping(target = "targetWarehouseName", source = "targetWarehouse.name")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    TransferResponseDTO toResponseDTO(Transfer entity);

    @Mapping(target = "productUuid", source = "product.uuid")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    TransferItemResponseDTO itemToResponseDTO(TransferItem entity);
}