package com.warnes.erp.modules.sales.mapper;

import com.warnes.erp.modules.sales.dto.request.PriceListRequestDTO;
import com.warnes.erp.modules.sales.dto.response.PriceListResponseDTO;
import com.warnes.erp.modules.sales.entity.PriceList;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceListMapper {

    PriceList toEntity(PriceListRequestDTO request);

    PriceListResponseDTO toResponseDTO(PriceList entity);

    void updateEntityFromDto(PriceListRequestDTO request, @MappingTarget PriceList entity);
}