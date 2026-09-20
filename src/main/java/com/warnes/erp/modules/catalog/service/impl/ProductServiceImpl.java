package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BulkImportResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.entity.Brand;
import com.warnes.erp.modules.catalog.entity.Category;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.entity.ProductCrossReference;
import com.warnes.erp.modules.catalog.mapper.ProductMapper;
import com.warnes.erp.modules.catalog.repository.BrandRepository;
import com.warnes.erp.modules.catalog.repository.CategoryRepository;
import com.warnes.erp.modules.catalog.repository.ProductCrossReferenceRepository;
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final ProductCrossReferenceRepository crossReferenceRepository;

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new BusinessRuleException("El SKU ingresado ya pertenece a otro producto activo.");
        }

        Product product = productMapper.toEntity(request);
        assignCategoryAndBrand(product, request.categoryUuid(), request.brandUuid());
        handleCrossReferences(product, request);

        return productMapper.toResponseDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(UUID uuid, ProductRequestDTO request) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));

        if (!product.getSku().equalsIgnoreCase(request.sku()) &&
                productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new BusinessRuleException("El nuevo SKU ingresado ya pertenece a otro producto activo.");
        }

        product.setSku(request.sku());
        product.setBarcode(request.barcode());
        product.setName(request.name());
        product.setService(request.service());
        product.setKit(request.kit());
        product.setCostPrice(request.costPrice());
        product.setProfitMargin(request.profitMargin());
        product.setSalePrice(request.salePrice());
        product.setTaxPercentage(request.taxPercentage());

        assignCategoryAndBrand(product, request.categoryUuid(), request.brandUuid());

        product.getCrossReferences().clear();
        handleCrossReferences(product, request);

        return productMapper.toResponseDTO(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductByUuid(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));
        return productMapper.toResponseDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByCode(String query) {
        return productRepository.searchBySkuOrEquivalence(query).stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProduct(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));

        product.setDeletedAt(ZonedDateTime.now());
        product.getCrossReferences().forEach(cr -> cr.setDeletedAt(ZonedDateTime.now()));
        productRepository.save(product);
    }

    private void assignCategoryAndBrand(Product product, UUID categoryUuid, UUID brandUuid) {
        Category category = categoryRepository.findByUuid(categoryUuid)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría especificada no existe."));
        Brand brand = brandRepository.findByUuid(brandUuid)
                .orElseThrow(() -> new ResourceNotFoundException("La marca especificada no existe."));

        product.setCategory(category);
        product.setBrand(brand);
    }

    private void handleCrossReferences(Product product, ProductRequestDTO request) {
        if (request.crossReferences() != null) {
            List<ProductCrossReference> newRefs = request.crossReferences().stream()
                    .map(dto -> {
                        ProductCrossReference cr = productMapper.toCrossReferenceEntity(dto);
                        cr.setProduct(product);
                        return cr;
                    }).toList();
            product.getCrossReferences().addAll(newRefs);
        }
    }

    /**
     * Motor Bulk: Procesa un archivo CSV (NUESTRO_SKU, MARCA_EXTERNA, SKU_EXTERNO)
     */
    @Override
    @Transactional
    public BulkImportResponseDTO importCrossReferencesCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("El archivo CSV está vacío.");
        }

        int total = 0, success = 0, failed = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; } // Saltar cabecera

                total++;
                String[] columns = line.split(",");
                if (columns.length < 3) { failed++; continue; }

                String internalSku = columns[0].trim();
                String extBrand = columns[1].trim();
                String extSku = columns[2].trim();

                Optional<Product> productOpt = productRepository.findBySkuIgnoreCase(internalSku);

                if (productOpt.isPresent()) {
                    Product product = productOpt.get();

                    // Prevenir duplicados exactos
                    boolean exists = product.getCrossReferences().stream()
                            .anyMatch(cr -> cr.getExternalSku().equalsIgnoreCase(extSku) && cr.getExternalBrand().equalsIgnoreCase(extBrand));

                    if (!exists) {
                        ProductCrossReference ref = new ProductCrossReference();
                        ref.setProduct(product);
                        ref.setExternalBrand(extBrand);
                        ref.setExternalSku(extSku);

                        crossReferenceRepository.save(ref);
                        success++;
                    } else {
                        failed++; // Ya existía
                    }
                } else {
                    failed++; // No tenemos el producto interno base
                }
            }
        } catch (Exception e) {
            log.error("Error procesando CSV de equivalencias", e);
            throw new BusinessRuleException("Error crítico al leer el archivo: " + e.getMessage());
        }

        return new BulkImportResponseDTO(total, success, failed, "Proceso de importación finalizado.");
    }
}