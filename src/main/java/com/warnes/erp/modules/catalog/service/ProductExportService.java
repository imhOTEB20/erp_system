package com.warnes.erp.modules.catalog.service;

import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import java.util.List;

public interface ProductExportService {
    byte[] exportProductsToExcel(List<ProductResponseDTO> products);
}