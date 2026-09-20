package com.warnes.erp.modules.purchases.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.entity.TaxType;
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.catalog.repository.TaxTypeRepository;
import com.warnes.erp.modules.inventory.entity.MovementType;
import com.warnes.erp.modules.inventory.entity.Stock;
import com.warnes.erp.modules.inventory.entity.StockMovement;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.repository.StockRepository;
import com.warnes.erp.modules.inventory.repository.StockMovementRepository;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.purchases.dto.request.PurchaseCreateRequestDTO;
import com.warnes.erp.modules.purchases.dto.request.PurchaseItemRequestDTO;
import com.warnes.erp.modules.purchases.dto.request.PurchaseTaxRequestDTO;
import com.warnes.erp.modules.purchases.dto.response.PurchaseResponseDTO;
import com.warnes.erp.modules.purchases.entity.Purchase;
import com.warnes.erp.modules.purchases.entity.PurchaseItem;
import com.warnes.erp.modules.purchases.entity.PurchaseTax;
import com.warnes.erp.modules.purchases.entity.Supplier;
import com.warnes.erp.modules.purchases.mapper.PurchaseMapper;
import com.warnes.erp.modules.purchases.repository.PurchaseRepository;
import com.warnes.erp.modules.purchases.repository.SupplierRepository;
import com.warnes.erp.modules.purchases.service.PurchaseService;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final TaxTypeRepository taxTypeRepository;

    private final PurchaseMapper purchaseMapper;

    @Override
    @Transactional
    public PurchaseResponseDTO createPurchase(PurchaseCreateRequestDTO request) {
        Supplier supplier = supplierRepository.findByUuid(request.supplierUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        if (!supplier.isActive()) {
            throw new BusinessRuleException("No se pueden registrar compras a un proveedor inactivo");
        }

        if (purchaseRepository.existsBySupplierIdAndInvoiceNumberIgnoreCase(supplier.getId(), request.invoiceNumber())) {
            throw new BusinessRuleException("Ya existe una factura registrada con ese número para este proveedor");
        }

        Warehouse destination = warehouseRepository.findByUuid(request.destinationWarehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén de destino no encontrado"));

        User currentUser = getCurrentAuthenticatedUser();

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setDestinationWarehouse(destination);
        purchase.setInvoiceNumber(request.invoiceNumber());
        purchase.setTotalAmount(request.totalAmount());

        // Guardamos la cabecera primero para que Hibernate/PostgreSQL genere el UUID
        purchase = purchaseRepository.save(purchase);

        for (PurchaseItemRequestDTO itemDto : request.items()) {
            Product product = productRepository.findByUuid(itemDto.productUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // 1. Actualizar el costo del producto basado en la última compra
            product.setCostPrice(itemDto.unitCost());
            productRepository.save(product);

            // 2. Bloqueo pesimista del inventario en el almacén destino
            Stock stock = stockRepository.findByProductAndWarehouseForUpdate(product.getId(), destination.getId())
                    .orElseGet(() -> createInitialStock(product, destination));

            // Cálculo preciso
            BigDecimal newBalance = stock.getQuantity().add(itemDto.quantity());
            stock.setQuantity(newBalance);
            stockRepository.save(stock);

            // 3. Registrar el movimiento inmutable (con UUID de la compra como referenceDocument)
            recordKardexMovement(product, destination, currentUser, itemDto.quantity(), newBalance, MovementType.PURCHASE, purchase.getUuid());

            // 4. Agregar ítem a la factura de compra
            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setPurchase(purchase);
            purchaseItem.setProduct(product);
            purchaseItem.setQuantity(itemDto.quantity());
            purchaseItem.setUnitCost(itemDto.unitCost());
            purchase.getItems().add(purchaseItem);
        }

        // NUEVO: Procesamiento de Impuestos Aplicados
        if (request.taxes() != null && !request.taxes().isEmpty()) {
            for (PurchaseTaxRequestDTO taxDto : request.taxes()) {
                TaxType taxType = taxTypeRepository.findByUuid(taxDto.taxTypeUuid())
                        .orElseThrow(() -> new ResourceNotFoundException("Tipo de impuesto no encontrado"));

                if (!taxType.isActive()) {
                    throw new BusinessRuleException("El impuesto " + taxType.getName() + " se encuentra inactivo");
                }

                PurchaseTax purchaseTax = new PurchaseTax();
                purchaseTax.setPurchase(purchase); // Vinculamos a la factura raíz
                purchaseTax.setTaxType(taxType);
                purchaseTax.setTaxAmount(taxDto.taxAmount());

                purchase.getTaxes().add(purchaseTax);
            }
        }

        return purchaseMapper.toResponseDTO(purchaseRepository.save(purchase));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponseDTO getPurchaseByUuid(UUID uuid) {
        Purchase purchase = purchaseRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));
        return purchaseMapper.toResponseDTO(purchase);
    }

    private Stock createInitialStock(Product product, Warehouse warehouse) {
        Stock newStock = new Stock();
        newStock.setProduct(product);
        newStock.setWarehouse(warehouse);
        newStock.setQuantity(BigDecimal.ZERO);
        return newStock;
    }

    private void recordKardexMovement(Product product, Warehouse warehouse, User user, BigDecimal quantity, BigDecimal balanceAfter, MovementType type, UUID referenceDocument) {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setUser(user);
        movement.setMovementType(type);

        // Conversión y trazabilidad obligatoria
        movement.setQuantity(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceDocument(referenceDocument);

        stockMovementRepository.save(movement);
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de auditoría no encontrado"));
    }
}