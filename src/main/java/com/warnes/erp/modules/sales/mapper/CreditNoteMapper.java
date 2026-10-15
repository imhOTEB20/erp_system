package com.warnes.erp.modules.sales.mapper;

import com.warnes.erp.modules.sales.dto.response.CreditNoteItemResponseDTO;
import com.warnes.erp.modules.sales.dto.response.CreditNoteResponseDTO;
import com.warnes.erp.modules.sales.entity.CreditNote;
import com.warnes.erp.modules.sales.entity.CreditNoteItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreditNoteMapper {

    @Mapping(target = "saleUuid", source = "sale.uuid")
    @Mapping(target = "saleInvoiceNumber", source = "sale.invoiceNumber")
    @Mapping(target = "customerUuid", source = "customer.uuid")
    @Mapping(target = "customerName", source = "customer.businessName")
    @Mapping(target = "refundType", expression = "java(entity.getRefundType().name())")
    CreditNoteResponseDTO toResponseDTO(CreditNote entity);

    @Mapping(target = "productUuid", source = "product.uuid")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    CreditNoteItemResponseDTO itemToResponseDTO(CreditNoteItem entity);
}