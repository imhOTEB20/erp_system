package com.warnes.erp.modules.inventory.mapper;

import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;
import com.warnes.erp.modules.inventory.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockMovementMapper {

    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "userName", source = "user.fullName") // O user.username dependiendo de qué quieras mostrar
    StockMovementResponseDTO toResponseDTO(StockMovement entity);
}