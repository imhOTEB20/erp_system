package com.warnes.erp.modules.inventory.mapper;

import com.warnes.erp.modules.inventory.dto.response.ProductStockResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.StockResponseDTO;
import com.warnes.erp.modules.inventory.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockMapper {

    @Mapping(target = "productUuid", source = "product.uuid")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "stockResponseDTO", source = ".") // Delega el mapeo al método de abajo
    ProductStockResponseDTO toProductStockResponseDTO(Stock entity);

    // Mapea directamente los campos 'quantity' y 'minStock' de la entidad
    StockResponseDTO toStockResponseDTO(Stock entity);
}