package com.warnes.erp.modules.inventory.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.activity.entity.ActivityType;
import com.warnes.erp.modules.activity.event.ErpRealtimeEvents;
import com.warnes.erp.modules.activity.service.ActivityLogService;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.inventory.dto.request.TransferCreateRequestDTO;
import com.warnes.erp.modules.inventory.dto.request.TransferItemRequestDTO;
import com.warnes.erp.modules.inventory.dto.response.TransferResponseDTO;
import com.warnes.erp.modules.inventory.entity.*;
import com.warnes.erp.modules.inventory.mapper.TransferMapper;
import com.warnes.erp.modules.inventory.repository.*;
import com.warnes.erp.modules.inventory.service.StockService;
import com.warnes.erp.modules.inventory.service.TransferService;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final StockServiceImpl stockService;
    private final ActivityLogService activityLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransferResponseDTO createTransfer(TransferCreateRequestDTO request) {

        // 1. Validar que origen y destino no sean el mismo
        if (request.originWarehouseUuid().equals(request.targetWarehouseUuid())) {
            throw new BusinessRuleException("El almacén de origen y destino no pueden ser el mismo.");
        }

        Warehouse origin = warehouseRepository.findByUuid(request.originWarehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén de origen no encontrado."));

        Warehouse target = warehouseRepository.findByUuid(request.targetWarehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén de destino no encontrado."));

        User currentUser = getCurrentAuthenticatedUser();

        // 2. Crear la entidad principal con generación de código
        Transfer transfer = new Transfer();
        transfer.setTrackingCode(generateUniqueTrackingCode());
        transfer.setOriginWarehouse(origin);
        transfer.setTargetWarehouse(target);
        transfer.setUser(currentUser);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setItems(new ArrayList<>());

        // 3. Procesar ítems y descontar stock del almacén origen
        request.items().forEach(itemDto -> {
            Product product = productRepository.findByUuid(itemDto.productUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + itemDto.productUuid()));

            // Bloqueo pesimista (ForUpdate) en BD para evitar ventas simultáneas que rompan el stock
            Specification<Stock> filter = StockSpecification.hasWarehouseUuid(origin.getUuid())
                    .and(StockSpecification.hasProductUuid(product.getUuid()));
            Stock originStock = stockRepository.findOne(filter).orElseThrow(() -> new BusinessRuleException("No hay stock registrado del producto en el almacén de origen."));

            if (originStock.getQuantity().compareTo(itemDto.quantity()) < 0) {
                throw new BusinessRuleException("Stock insuficiente en origen para el producto: " + product.getSku());
            }

            // Descontar inventario (Reserva de la transferencia)
            originStock.setQuantity(originStock.getQuantity().subtract(itemDto.quantity()));
            stockRepository.save(originStock);

            // Registrar movimiento en el Kardex (Salida por Transferencia)
            // Llama a tu método interno que inserta en stock_movements
            stockService.recordKardexMovement(product, origin, currentUser, itemDto.quantity(),
                    originStock.getQuantity(), MovementType.TRANSFER_OUT, transfer.getUuid());

            // Armar ítem
            TransferItem transferItem = new TransferItem();
            transferItem.setTransfer(transfer);
            transferItem.setProduct(product);
            transferItem.setQuantity(itemDto.quantity());

            transfer.getItems().add(transferItem);
        });

        Transfer savedTransfer = transferRepository.save(transfer);

        activityLogService.recordDomainActivity(
                ActivityType.TRANSFER_CREATED,
                "INTENTARIO",
                String.format("Transferencia de %s a %s",
                        savedTransfer.getOriginWarehouse().getName(),
                        savedTransfer.getTargetWarehouse().getName()),
                String.format("Transferencia registrada con %d ítems desde '%s' hacia '%s' (Estado: %s)",
                        savedTransfer.getItems().size(),
                        savedTransfer.getOriginWarehouse().getName(),
                        savedTransfer.getTargetWarehouse().getName(),
                        savedTransfer.getStatus()),
                savedTransfer.getUuid(),
                savedTransfer.getTrackingCode(),
                savedTransfer.getItems().stream()
                        .map(TransferItem::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                savedTransfer.getOriginWarehouse(),
                savedTransfer.getTargetWarehouse(),
                null,
                null
        );

        eventPublisher.publishEvent(new ErpRealtimeEvents.TransferStateDomainEvent(
                "TRANSFER_" + savedTransfer.getStatus().name(), // "TRANSFER_PENDING", "TRANSFER_COMPLETED", "TRANSFER_CANCELLED"
                savedTransfer.getUuid(),
                savedTransfer.getStatus(),
                savedTransfer.getOriginWarehouse().getUuid(),
                savedTransfer.getOriginWarehouse().getName(),
                savedTransfer.getTargetWarehouse().getUuid(),
                savedTransfer.getTargetWarehouse().getName()
        ));

        return transferMapper.toResponseDTO(savedTransfer);
    }

    /**
     * Genera un código único alfanumérico. Utiliza un do-while para garantizar
     * a nivel de BD que no haya colisiones antes de intentar insertar.
     */
    private String generateUniqueTrackingCode() {
        String code;
        do {
            // Genera un código formato: TRX-A1B2C3D4
            code = "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (transferRepository.exists(TransferSpecification.hasTrackingCode(code)));
        return code;
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponseDTO getTransferByUuid(UUID uuid) {
        return transferMapper.toResponseDTO(findTransferByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponseDTO getTransferByTrackingCode(String trackingCode) {
        Transfer transfer = transferRepository.findOne(TransferSpecification.hasTrackingCode(trackingCode))
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia no encontrada"));
        return transferMapper.toResponseDTO(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getAll() {
        return transferRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(transferMapper::toResponseDTO)
                .collect(Collectors.toList());
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
            Specification<Stock> filter = StockSpecification.hasWarehouseUuid(target.getUuid())
                    .and(StockSpecification.hasProductUuid(item.getProduct().getUuid()));
            Stock targetStock = stockRepository.findOne(filter)
                    .orElseGet(() -> createInitialStock(item.getProduct(), target));

            // Ingresamos stock al destino
            BigDecimal newBalance = targetStock.getQuantity().add(item.getQuantity());
            targetStock.setQuantity(newBalance);
            stockRepository.save(targetStock);

            recordKardexMovement(item.getProduct(), target, currentUser, item.getQuantity(), newBalance, MovementType.TRANSFER_IN, transfer.getUuid());
        }

        transfer.setStatus(TransferStatus.COMPLETED);

        Transfer savedTransfer = transferRepository.save(transfer);

        activityLogService.recordDomainActivity(
                ActivityType.TRANSFER_DISPATCHED,
                "INVENTORIO",
                String.format("Transferencia de %s a %s",
                        savedTransfer.getOriginWarehouse().getName(),
                        savedTransfer.getTargetWarehouse().getName()),
                String.format("Transferencia registrada con %d ítems desde '%s' hacia '%s' (Estado: %s)",
                        savedTransfer.getItems().size(),
                        savedTransfer.getOriginWarehouse().getName(),
                        savedTransfer.getTargetWarehouse().getName(),
                        savedTransfer.getStatus()),
                savedTransfer.getUuid(),
                savedTransfer.getTrackingCode(),
                savedTransfer.getItems().stream()
                        .map(TransferItem::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                savedTransfer.getOriginWarehouse(),
                savedTransfer.getTargetWarehouse(),
                null,
                null
        );

        return transferMapper.toResponseDTO(savedTransfer);
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
            Specification<Stock> filter = StockSpecification.hasWarehouseUuid(origin.getUuid())
                    .and(StockSpecification.hasProductUuid(item.getProduct().getUuid()));
            Stock originStock = stockRepository.findOne(filter)
                    .orElseThrow(() -> new BusinessRuleException("Error de consistencia en el almacén de origen"));

            BigDecimal newBalance = originStock.getQuantity().add(item.getQuantity());
            originStock.setQuantity(newBalance);
            stockRepository.save(originStock);

            recordKardexMovement(item.getProduct(), origin, currentUser, item.getQuantity(), newBalance, MovementType.RETURN_IN, transfer.getUuid());
        }

        transfer.setStatus(TransferStatus.CANCELLED);

        Transfer savedTransfer = transferRepository.save(transfer);

        activityLogService.recordDomainActivity(
                ActivityType.TRANSFER_CANCELLED,
                "INVENTORIO",
                String.format("Transferencia de %s a %s",
                        savedTransfer.getOriginWarehouse().getName(),
                        savedTransfer.getTargetWarehouse().getName()),
                String.format("Transferencia registrada con %d ítems desde '%s' hacia '%s' (Estado: %s)",
                        savedTransfer.getItems().size(),
                        savedTransfer.getOriginWarehouse().getName(),
                        savedTransfer.getTargetWarehouse().getName(),
                        savedTransfer.getStatus()),
                savedTransfer.getUuid(),
                savedTransfer.getTrackingCode(),
                savedTransfer.getItems().stream()
                        .map(TransferItem::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                savedTransfer.getOriginWarehouse(),
                savedTransfer.getTargetWarehouse(),
                null,
                null
        );
        return transferMapper.toResponseDTO(savedTransfer);
    }

    private Transfer findTransferByUuid(UUID uuid) {
        return transferRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia no encontrada"));
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