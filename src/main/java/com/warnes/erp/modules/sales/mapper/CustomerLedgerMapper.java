package com.warnes.erp.modules.sales.mapper;

import com.warnes.erp.modules.sales.dto.response.CustomerLedgerResponseDTO;
import com.warnes.erp.modules.sales.entity.CustomerLedger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerLedgerMapper {

    @Mapping(target = "saleUuid", source = "sale.uuid")
    @Mapping(target = "invoiceNumber", source = "sale.invoiceNumber")
    CustomerLedgerResponseDTO toResponseDTO(CustomerLedger entity);
}