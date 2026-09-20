package com.warnes.erp.modules.inventory.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.inventory.dto.request.TransferCreateRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.TransferItemRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.TransferResponseDTO;
import com.warnes.erp.modules.inventory.entity.*;
import com.warnes.erp.modules.inventory.mapper.TransferMapper;
import com.warnes.erp.modules.inventory.repository.StockRepository;
import com.warnes.erp.modules.inventory.repository.StockMovementRepository;
import com.warnes.erp.modules.inventory.repository.TransferRepository;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.inventory.service.TransferService;
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
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final TransferMapper transferMapper;

    @Override
    @Transactional
    public TransferResponseDTO createTransfer(TransferCreateRequestDTO request) {
        if (request.originWarehouseUuid().equals(request.targetWarehouseUuid())) {
            throw new BusinessRuleException("El almacén de origen y destino no pueden ser el mismo");
        }
        if (transferRepository.existsByTrackingCodeIgnoreCase(request.trackingCode())) {
            throw new BusinessRuleException("El código de seguimiento ya existe");
        }

        Warehouse origin = findWarehouseByUuid(request.originWarehouseUuid());
        Warehouse target = findWarehouseByUuid(request.targetWarehouseUuid());
        User currentUser = getCurrentAuthenticatedUser();

        Transfer transfer = new Transfer();
        transfer.setTrackingCode(request.trackingCode());
        transfer.setOriginWarehouse(origin);
        transfer.setTargetWarehouse(target);
        transfer.setStatus(TransferStatus.PENDING);

        for (TransferItemRequestDTO itemDto : request.items()) {
            Product product = productRepository.findByUuid(itemDto.productUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // Bloqueo pesimista en origen para garantizar stock disponible
            Stock originStock = stockRepository.findByProductAndWarehouseForUpdate(product.getId(), origin.getId())
                    .orElseThrow(() -> new BusinessRuleException("No hay stock registrado del producto en el almacén de origen"));

            if (originStock.getQuantity().compareTo(itemDto.quantity()) < 0) {
                throw new BusinessRuleException("Stock insuficiente para " + product.getSku() + ". Disponible: " + originStock.getQuantity());
            }

            // Descontamos stock de origen
            BigDecimal newBalance = originStock.getQuantity().subtract(itemDto.quantity());
            originStock.setQuantity(newBalance);
            stockRepository.save(originStock);

            TransferItem transferItem = new TransferItem();
            transferItem.setTransfer(transfer);
            transferItem.setProduct(product);
            transferItem.setQuantity(itemDto.quantity());
            transfer.getItems().add(transferItem);

            recordKardexMovement(product, origin, currentUser, itemDto.quantity(), newBalance, MovementType.TRANSFER_OUT, transfer.getUuid());
        }

        return transferMapper.toResponseDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponseDTO getTransferByUuid(UUID uuid) {
        return transferMapper.toResponseDTO(findTransferByUuid(uuid));
    }

    @Override
    @Transactional
    public TransferResponseDTO completeTransfer(UUID uuid) {
        Transfer transfer = findTransferByUuid(uuid);
        validateStatusForUpdate(transfer, TransferStatus.PENDING);

        User currentUser = getCurrentAuthenticatedUser();
        Warehouse target = transfer.getTargetWarehouse();

        for (TransferItem item : transfer.getItems()) {
            // Bloqueo pesimista en destino
            Stock targetStock = stockRepository.findByProductAndWarehouseForUpdate(item.getProduct().getId(), target.getId())
                    .orElseGet(() -> createInitialStock(item.getProduct(), target));

            // Ingresamos stock al destino
            BigDecimal newBalance = targetStock.getQuantity().add(item.getQuantity());
            targetStock.setQuantity(newBalance);
            stockRepository.save(targetStock);

            recordKardexMovement(item.getProduct(), target, currentUser, item.getQuantity(), newBalance, MovementType.TRANSFER_IN, transfer.getUuid());
        }

        transfer.setStatus(TransferStatus.COMPLETED);
        return transferMapper.toResponseDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public TransferResponseDTO cancelTransfer(UUID uuid) {
        Transfer transfer = findTransferByUuid(uuid);
        validateStatusForUpdate(transfer, TransferStatus.PENDING);

        User currentUser = getCurrentAuthenticatedUser();
        Warehouse origin = transfer.getOriginWarehouse();

        for (TransferItem item : transfer.getItems()) {
            // Devolvemos el stock al origen
            Stock originStock = stockRepository.findByProductAndWarehouseForUpdate(item.getProduct().getId(), origin.getId())
                    .orElseThrow(() -> new BusinessRuleException("Error de consistencia en el almacén de origen"));

            BigDecimal newBalance = originStock.getQuantity().add(item.getQuantity());
            originStock.setQuantity(newBalance);
            stockRepository.save(originStock);

            recordKardexMovement(item.getProduct(), origin, currentUser, item.getQuantity(), newBalance, MovementType.RETURN_IN, transfer.getUuid());
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        return transferMapper.toResponseDTO(transferRepository.save(transfer));
    }

    private Transfer findTransferByUuid(UUID uuid) {
        return transferRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia no encontrada"));
    }

    private Warehouse findWarehouseByUuid(UUID uuid) {
        return warehouseRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));
    }

    private void validateStatusForUpdate(Transfer transfer, TransferStatus expectedStatus) {
        if (transfer.getStatus() != expectedStatus) {
            throw new BusinessRuleException("La transferencia no está en estado " + expectedStatus + " (Estado actual: " + transfer.getStatus() + ")");
        }
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