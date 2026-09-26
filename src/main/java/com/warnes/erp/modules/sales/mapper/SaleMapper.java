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
            @Mapping(target = "userUuid", source = "user.uuid"),        // NUEVO
            @Mapping(target = "userName", source = "user.fullName"),    // NUEVO
            @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    })
    SaleResponseDTO toResponseDTO(Sale entity);
}