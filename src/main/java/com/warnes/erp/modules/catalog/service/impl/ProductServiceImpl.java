package com.warnes.erp.modules.catalog.service.impl;

import com.lowagie.text.Row;
import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ElementNotDeletedException;
import com.warnes.erp.exception.InceptionException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.dto.request.KitItemRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductRequestDTO;
import com.warnes.erp.modules.catalog.dto.request.ProductUpdateRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.BulkImportResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.dto.response.ProductSimpleResponseDTO;
import com.warnes.erp.modules.catalog.entity.*;
import com.warnes.erp.modules.catalog.mapper.ProductMapper;
import com.warnes.erp.modules.catalog.repository.*;
import com.warnes.erp.modules.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final TaxTypeRepository taxTypeRepository;
    private final ProductMapper productMapper;
    private final ProductCrossReferenceRepository crossReferenceRepository;

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        String finalSku = request.sku();

        // REGLA DE NEGOCIO: Generación de SKU Interno si viene vacío
        if (finalSku == null || finalSku.isBlank()) {
            finalSku = "REP-" + RandomStringUtils.randomAlphanumeric(8).toUpperCase();
        } else if (productRepository.existsBySkuIgnoreCase(finalSku)) {
            throw new BusinessRuleException("El SKU ingresado ya pertenece a otro producto activo.");
        }

        Product product = productMapper.toEntity(request);
        product.setSku(finalSku); // Asignamos el SKU definitivo

        assignMasterData(product, request.categoryUuid(), request.brandUuid(), request.taxTypeUuid());
        assignProductsKit(product, request);
        handleCrossReferences(product, request);

        return productMapper.toResponseDTO(productRepository.save(product));
    }

    private void assignProductsKit(Product product, ProductRequestDTO request) {
        if (Boolean.TRUE.equals(request.kit())) {
            if (request.kitItems() == null || request.kitItems().isEmpty()) {
                throw new BusinessRuleException("Un producto marcado como Kit debe contener al menos un componente (kitItems).");
            }

            for (KitItemRequestDTO itemDto : request.kitItems()) {
                Product childProduct = productRepository.findByUuid(itemDto.childProductUuid())
                        .orElseThrow(() -> new ResourceNotFoundException("Componente del kit no encontrado: " + itemDto.childProductUuid()));

                // Anti-Inception: Un kit no puede tener otro kit adentro
                if (childProduct.isKit()) {
                    throw new InceptionException("Un Kit no puede contener a otro Kit en su interior (SKU: " + childProduct.getSku() + ").");
                }

                KitItem kitItem = new KitItem();
                kitItem.setParentProduct(product); // Importante: Asociar el padre antes de guardar
                kitItem.setChildProduct(childProduct);
                kitItem.setQuantity(itemDto.quantity());

                product.getKitItems().add(kitItem);
            }
        } else {
            // Protección contra datos sucios si el frontend envía items pero marca kit = false
            if (request.kitItems() != null && !request.kitItems().isEmpty()) {
                throw new BusinessRuleException("No se pueden asignar componentes a un producto que no está marcado como Kit.");
            }
        }
    }

    // Método centralizado para poblar las 3 relaciones maestras
    private void assignMasterData(Product product, UUID categoryUuid, UUID brandUuid, UUID taxTypeUuid) {
        Category category = categoryRepository.findByUuid(categoryUuid)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría especificada no existe."));
        Brand brand = brandRepository.findByUuid(brandUuid)
                .orElseThrow(() -> new ResourceNotFoundException("La marca especificada no existe."));
        TaxType taxType = taxTypeRepository.findByUuid(taxTypeUuid)
                .orElseThrow(() -> new ResourceNotFoundException("El tipo de impuesto especificado no existe."));

        product.setCategory(category);
        product.setBrand(brand);
        product.setTaxType(taxType);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(UUID uuid, ProductUpdateRequestDTO request) {
        Specification<Product> filtro = Specification
                .where(ProductSpecification.notDeleted());
        Product product = findByUuid(uuid);

        // 1. MapStruct actualiza los tipos simples (String, BigDecimal, Boolean) omitiendo los nulos
        productMapper.updateEntityFromDto(request, product);

        // 2. Lógica Manual Condicional para Claves Foráneas
        if (request.categoryUuid() != null) {
            product.setCategory(categoryRepository.findByUuid(request.categoryUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada.")));
        }

        if (request.brandUuid() != null) {
            product.setBrand(brandRepository.findByUuid(request.brandUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada.")));
        }

        if (request.taxTypeUuid() != null) {
            product.setTaxType(taxTypeRepository.findByUuid(request.taxTypeUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de impuesto no encontrado.")));
        }

        return productMapper.toResponseDTO(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProducts(UUID categoryUuid, UUID brandUuid, Boolean isKit, Boolean isService, Boolean isActive, Boolean includeDeleted, Pageable pageable) {
        Specification<Product> filters = ProductSpecification.withDynamicFilters(categoryUuid, brandUuid, isKit, isService, isActive, includeDeleted);
        Page<Product> pagedProducts = productRepository.findAll(filters, pageable);
        return pagedProducts.map(productMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsForExport() {
        Specification<Product> filter = ProductSpecification
                .notDeleted();
        return productRepository.findAll(filter).stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSimpleResponseDTO> getListProductNames(Boolean includeDeleted) {
        Specification<Product> filter = ProductSpecification.withIncludeDeleted(includeDeleted);

        return productRepository.findAll(filter).stream()
                .map(productMapper::toProductSimpleResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductByUuid(UUID uuid) {
        Product product = findByUuid(uuid);
        return productMapper.toResponseDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllDeleted() {
        Specification<Product> filter = ProductSpecification.deleted();
        return productRepository.findAll(filter).stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean toggleAvailability(UUID uuid) {
        Product product = findByUuid(uuid);
        product.setActive(!product.isActive());

        productRepository.save(product);
        return product.isActive();
    }

    @Override
    @Transactional
    public ProductResponseDTO restoreProduct(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));

        if (product.getDeletedAt() != null) {
            product.setDeletedAt(null);
            product.setActive(true);
        } else {
            throw new ElementNotDeletedException("El producto no esta eliminado.");
        }

        return productMapper.toResponseDTO(
                productRepository.save(product)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchByCode(String query, UUID categoryUuid, UUID brandUuid, Boolean isKit, Boolean isService, Boolean isActive, Boolean includeDeleted, Pageable pageable) {
        Specification<Product> filters = Specification
                .where(ProductSpecification.matchesSkuOrEquivalenceOrName(query, isKit, isService, isActive, includeDeleted));
        Page<Product> pagedProducts = productRepository.findAll(filters, pageable);
        return pagedProducts.map(productMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID uuid) {
        Product product = findByUuid(uuid);

        product.setDeletedAt(ZonedDateTime.now());
        product.setActive(false);
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

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductBySku(String sku) {
        Specification<Product> filter = Specification
                .where(ProductSpecification.hasSku(sku))
                .and(ProductSpecification.notDeleted());

        return productMapper.toResponseDTO(productRepository.findOne(filter)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado")));
    }

    private Product findByUuid(UUID uuid) {
        Specification<Product> filter = Specification
                .where(ProductSpecification.hasUuid(uuid))
                .and(ProductSpecification.notDeleted());
        return productRepository.findOne(filter)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));
    }

}