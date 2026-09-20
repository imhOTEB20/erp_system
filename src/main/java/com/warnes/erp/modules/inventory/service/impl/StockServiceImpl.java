package com.warnes.erp.modules.inventory.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.inventory.dto.request.StockAdjustmentRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;
import com.warnes.erp.modules.inventory.entity.MovementType;
import com.warnes.erp.modules.inventory.entity.Stock;
import com.warnes.erp.modules.inventory.entity.StockMovement;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.mapper.StockMovementMapper;
import com.warnes.erp.modules.inventory.repository.StockRepository;
import com.warnes.erp.modules.inventory.repository.StockMovementRepository;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.inventory.service.StockService;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final StockMovementMapper stockMovementMapper;

    @Override
    @Transactional
    public void adjustStock(StockAdjustmentRequestDTO request) {
        Product product = productRepository.findByUuid(request.productUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Warehouse warehouse = warehouseRepository.findByUuid(request.warehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));

        User currentUser = getCurrentAuthenticatedUser();

        Stock stock = stockRepository.findByProductAndWarehouseForUpdate(product.getId(), warehouse.getId())
                .orElseGet(() -> createInitialStock(product, warehouse));

        applyMovement(stock, request.movementType(), request.quantity());
        stockRepository.save(stock);

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

    private void recordKardexMovement(Product product, Warehouse warehouse, User user, BigDecimal quantity, BigDecimal balanceAfter, MovementType type, UUID referenceDocument) {
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
        if (!productRepository.findByUuid(productUuid).isPresent()) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }

        return stockMovementRepository.findByProductUuidOrderByCreatedAtDesc(productUuid)
                .stream()
                .map(stockMovementMapper::toResponseDTO)
                .toList();
    }
}