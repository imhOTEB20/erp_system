package com.warnes.erp.modules.inventory.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.exception.StockAlreadyInitializedException;
import com.warnes.erp.modules.activity.event.ErpRealtimeEvents;
import com.warnes.erp.modules.activity.service.ActivityLogService;
import com.warnes.erp.modules.catalog.dto.response.ProductSimpleResponseDTO;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.mapper.ProductMapper;
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.catalog.repository.ProductSpecification;
import com.warnes.erp.modules.inventory.dto.request.InitialStockRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.StockAdjustmentRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.ProductStockAlertResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.ProductStockResponseDTO;
import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;
import com.warnes.erp.modules.inventory.entity.*;
import com.warnes.erp.modules.inventory.mapper.StockMapper;
import com.warnes.erp.modules.inventory.mapper.StockMovementMapper;
import com.warnes.erp.modules.inventory.repository.StockRepository;
import com.warnes.erp.modules.inventory.repository.StockMovementRepository;
import com.warnes.erp.modules.inventory.repository.StockSpecification;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.inventory.service.StockService;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    private final StockMovementMapper stockMovementMapper;
    private final StockMapper stockMapper;
    private final ProductMapper productMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void adjustStock(StockAdjustmentRequestDTO request) {
        Product product = productRepository.findByUuid(request.productUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Warehouse warehouse = warehouseRepository.findByUuid(request.warehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));

        User currentUser = getCurrentAuthenticatedUser();

        Specification<Stock> filter = StockSpecification.hasProductUuid(product.getUuid())
                .and(StockSpecification.hasWarehouseUuid(warehouse.getUuid()));

        Stock stock = stockRepository.findOne(filter)
                .orElseGet(() -> createInitialStock(product, warehouse));

        BigDecimal previousQuantity = stock.getQuantity();

        applyMovement(stock, request.movementType(), request.quantity());
        stockRepository.save(stock);

        eventPublisher.publishEvent(new ErpRealtimeEvents.StockChangedDomainEvent(
                stock.getWarehouse().getUuid(),
                stock.getWarehouse().getName(),
                stock.getProduct().getUuid(),
                stock.getProduct().getSku(),
                stock.getProduct().getName(),
                previousQuantity,
                stock.getQuantity(),
                stock.getMinStock(),
                OperationSource.ADJUSTMENT
        ));
        // stock.getQuantity() contiene el saldo exacto POST movimiento. Pasamos NULL en documentUuid porque un ajuste manual no tiene documento de respaldo.
        recordKardexMovement(product, warehouse, currentUser, request.quantity(), stock.getQuantity(), request.movementType(), null);
    }

    private void applyMovement(Stock stock, MovementType type, BigDecimal quantity) {
        switch (type) {
            case PURCHASE, RETURN_IN, ADJUSTMENT, TRANSFER_IN ->
                    stock.setQuantity(stock.getQuantity().add(quantity));

            case SALE, TRANSFER_OUT -> {
                if (stock.getQuantity().compareTo(quantity) < 0) {
                    throw new BusinessRuleException(
                            "Stock insuficiente. Disponible: " + stock.getQuantity() + " | Solicitado: " + quantity
                    );
                }
                stock.setQuantity(stock.getQuantity().subtract(quantity));
            }
        }
    }

    public void recordKardexMovement(Product product, Warehouse warehouse, User user, BigDecimal quantity, BigDecimal balanceAfter, MovementType type, UUID referenceDocument) {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setUser(user);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceDocument(referenceDocument);

        stockMovementRepository.save(movement);
    }

    private Stock createInitialStock(Product product, Warehouse warehouse) {
        Stock newStock = new Stock();
        newStock.setProduct(product);
        newStock.setWarehouse(warehouse);
        newStock.setQuantity(BigDecimal.ZERO);
        return newStock;
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario audit no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponseDTO> getProductKardex(UUID productUuid) {
        if (productRepository.findByUuid(productUuid).isEmpty()) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }

        return stockMovementRepository.getKardexByProductUuidBypassingSoftDeletes(productUuid)
                .stream()
                .map(stockMovementMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true) // Importante: readOnly para optimizar consultas
    public List<ProductStockResponseDTO> getProductsByWarehouse(UUID warehouseUuid) {
        Warehouse warehouse = warehouseRepository.findByUuid(warehouseUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));

        Specification<Stock> filter = StockSpecification.hasWarehouseUuid(warehouse.getUuid());

        return stockRepository.findAll(filter).stream()
                .map(stockMapper::toProductStockResponseDTO)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSimpleResponseDTO> getProductNonStockInWarehouse(UUID warehouseUuid) {
        Specification<Product> filter = ProductSpecification.withoutStockInWarehouse(warehouseUuid);

        List<Product> nonStockProducts = productRepository.findAll(filter
                .and(ProductSpecification.notDeleted()));

        return nonStockProducts.stream()
                .map(productMapper::toProductSimpleResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductStockResponseDTO initializerStock(InitialStockRequestDTO request) {
        Product product = productRepository.findByUuid(request.productUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Warehouse warehouse = warehouseRepository.findByUuid(request.warehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));

        Specification<Stock> filter = StockSpecification.hasWarehouseUuid(warehouse.getUuid())
                .and(StockSpecification.hasProductUuid(product.getUuid()));

        Optional<Stock> oldStock = stockRepository.findOne(filter);

        if (oldStock.isPresent()) {
            throw new StockAlreadyInitializedException(
                    "El stock en el almacén seleccionado para el producto ya está inicializado."
            );
        }

        User currentUser = getCurrentAuthenticatedUser();

        Stock newStock = new Stock();
        newStock.setProduct(product);
        newStock.setWarehouse(warehouse);
        newStock.setQuantity(request.quantity());
        newStock.setMinStock(request.minStock());

        stockRepository.save(newStock);

        // stock.getQuantity() contiene el saldo exacto POST movimiento. Pasamos NULL en documentUuid porque un ajuste manual no tiene documento de respaldo.
        recordKardexMovement(product, warehouse, currentUser, request.quantity(), newStock.getQuantity(), MovementType.ADJUSTMENT, null);

        return stockMapper.toProductStockResponseDTO(newStock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getProductNamesBelowMinStock(UUID warehouseUuid) {
        warehouseRepository.findByUuid(warehouseUuid)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la sucursal o depósito con UUID: " + warehouseUuid));

        return stockRepository.findAll(
                        StockSpecification.belowOrEqualMinStock(warehouseUuid),
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "product.name")
                )
                .stream()
                .map(stock -> stock.getProduct().getName())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductStockAlertResponseDTO> getProductsBelowMinStockDetailed(UUID warehouseUuid) {
        warehouseRepository.findByUuid(warehouseUuid)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la sucursal o depósito con UUID: " + warehouseUuid));

        return stockRepository.findAll(
                        StockSpecification.belowOrEqualMinStock(warehouseUuid),
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "product.name")
                )
                .stream()
                .map(stockMapper::toProductStockAlertResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductStockAlertResponseDTO> getZeroStockProductsByWarehouse(UUID warehouseUuid) {
        warehouseRepository.findByUuid(warehouseUuid)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la sucursal o depósito con UUID: " + warehouseUuid));

        return stockRepository.findAll(
                        StockSpecification.zeroStock(warehouseUuid),
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "product.name")
                )
                .stream()
                .map(stockMapper::toProductStockAlertResponseDTO)
                .toList();
    }
}