package com.warnes.erp.modules.purchases.mapper;

import com.warnes.erp.modules.purchases.dto.response.PurchaseItemResponseDTO;
import com.warnes.erp.modules.purchases.dto.response.PurchaseResponseDTO;
import com.warnes.erp.modules.purchases.dto.response.PurchaseTaxResponseDTO;
import com.warnes.erp.modules.purchases.entity.Purchase;
import com.warnes.erp.modules.purchases.entity.PurchaseItem;
import com.warnes.erp.modules.purchases.entity.PurchaseTax;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseMapper {

    @Mapping(target = "supplierUuid", source = "supplier.uuid")
    @Mapping(target = "supplierName", source = "supplier.legalName")
    @Mapping(target = "destinationWarehouseUuid", source = "destinationWarehouse.uuid")
    @Mapping(target = "destinationWarehouseName", source = "destinationWarehouse.name")
    PurchaseResponseDTO toResponseDTO(Purchase entity);

    @Mapping(target = "productUuid", source = "product.uuid")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    PurchaseItemResponseDTO itemToResponseDTO(PurchaseItem entity);

    @Mapping(target = "taxTypeUuid", source = "taxType.uuid")
    @Mapping(target = "taxTypeName", source = "taxType.name")
    PurchaseTaxResponseDTO taxToResponseDTO(PurchaseTax entity);
}