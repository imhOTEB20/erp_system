package com.warnes.erp.modules.purchases.mapper;

import com.warnes.erp.modules.purchases.dto.request.SupplierRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.SupplierResponseDTO;
import com.warnes.erp.modules.purchases.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {
    Supplier toEntity(SupplierRequestDTO request);
    SupplierResponseDTO toResponseDTO(Supplier entity);
    void updateEntityFromDto(SupplierRequestDTO request, @MappingTarget Supplier entity);
}