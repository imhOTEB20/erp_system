package com.warnes.erp.modules.catalog.service;

import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductUpdateRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BulkImportResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request);
    ProductResponseDTO updateProduct(UUID uuid, ProductUpdateRequestDTO request);
    ProductResponseDTO getProductByUuid(UUID uuid);
    List<ProductResponseDTO> getAllProducts();
    List<ProductResponseDTO> searchByCode(String query); // Renombrado para coincidir con tu Controller
    void deleteProduct(UUID uuid);
    BulkImportResponseDTO importCrossReferencesCsv(MultipartFile file); // Agregado para tu importación CSV
}