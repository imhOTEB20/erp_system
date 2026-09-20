package com.warnes.erp.modules.catalog.mapper;

import com.warnes.erp.modules.catalog.dto.request.TaxTypeRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.TaxTypeResponseDTO;
import com.warnes.erp.modules.catalog.entity.TaxType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaxTypeMapper {
    TaxTypeResponseDTO toResponseDTO(TaxType entity);
    TaxType toEntity(TaxTypeRequestDTO dto);
}