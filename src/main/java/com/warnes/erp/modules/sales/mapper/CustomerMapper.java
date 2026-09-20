package com.warnes.erp.modules.sales.mapper;

import com.warnes.erp.modules.sales.dto.request.CustomerRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CustomerResponseDTO;
import com.warnes.erp.modules.sales.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    Customer toEntity(CustomerRequestDTO request);

    @Mapping(target = "priceListUuid", source = "priceList.uuid")
    @Mapping(target = "priceListName", source = "priceList.name")
    CustomerResponseDTO toResponseDTO(Customer entity);

    void updateEntityFromDto(CustomerRequestDTO request, @MappingTarget Customer entity);
}