package com.warnes.erp.modules.sales.mapper;

import com.warnes.erp.modules.sales.dto.response.SaleItemResponseDTO;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.entity.SaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SaleMapper {

    @Mappings({
            @Mapping(target = "cashShiftUuid", source = "cashShift.uuid"),
            @Mapping(target = "customerUuid", source = "customer.uuid"),
            @Mapping(target = "customerName", source = "customer.businessName"),
            @Mapping(target = "userUuid", source = "user.uuid"),
            @Mapping(target = "userName", source = "user.fullName"),
            @Mapping(target = "status", expression = "java(entity.getStatus().name())"),

            // Extracción dinámica del almacén a través de la caja registradora.
            // MapStruct ignorará esto de forma segura si cashShift es nulo (Ventas B2B).
            @Mapping(target = "warehouseUuid", source = "cashShift.cashRegister.warehouse.uuid"),
            @Mapping(target = "warehouseName", source = "cashShift.cashRegister.warehouse.name")
    })
    SaleResponseDTO toResponseDTO(Sale entity);

    @Mapping(target = "productUuid", source = "product.uuid")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    SaleItemResponseDTO toItemResponseDTO(SaleItem entity);
}