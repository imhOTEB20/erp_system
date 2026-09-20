package com.warnes.erp.modules.catalog.mapper;

import com.warnes.erp.modules.catalog.dto.response.KitItemResponseDTO;
import com.warnes.erp.modules.catalog.entity.KitItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KitItemMapper {

    @Mapping(target = "childProductUuid", source = "childProduct.uuid")
    @Mapping(target = "childProductSku", source = "childProduct.sku")
    @Mapping(target = "childProductName", source = "childProduct.name")
    @Mapping(target = "unitCost", source = "childProduct.costPrice")
    @Mapping(target = "subtotalCost", source = ".", qualifiedByName = "calculateSubtotal")
    KitItemResponseDTO toResponseDTO(KitItem entity);

    @Named("calculateSubtotal")
    default BigDecimal calculateSubtotal(KitItem entity) {
        if (entity.getChildProduct() == null || entity.getChildProduct().getCostPrice() == null || entity.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return entity.getChildProduct().getCostPrice().multiply(entity.getQuantity());
    }
}