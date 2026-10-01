package com.warnes.erp.modules.catalog.service;

import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductUpdateRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BulkImportResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductSimpleResponseDTO;
import com.warnes.erp.modules.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request);
    ProductResponseDTO updateProduct(UUID uuid, ProductUpdateRequestDTO request);
    Page<ProductResponseDTO> getProducts(UUID categoryUuid, UUID brandUuid, Boolean isKit, Boolean isService, Boolean isActive, Boolean includeDeleted, Pageable pageable);
    List<ProductResponseDTO> getProductsForExport();
    List<ProductSimpleResponseDTO> getListProductNames(Boolean includeDeleted);
    ProductResponseDTO getProductByUuid(UUID uuid);
    List<ProductResponseDTO> getAllDeleted();
    Boolean toggleAvailability(UUID uuid);
    ProductResponseDTO restoreProduct(UUID uuid);
    Page<ProductResponseDTO> searchByCode(String query, UUID categoryUuid, UUID brandUuid, Boolean isKit, Boolean isService, Boolean isActive, Boolean includeDeleted, Pageable pageable); // Renombrado para coincidir con tu Controller
    void deleteProduct(UUID uuid);
    BulkImportResponseDTO importCrossReferencesCsv(MultipartFile file); // Agregado para tu importación CSV
}