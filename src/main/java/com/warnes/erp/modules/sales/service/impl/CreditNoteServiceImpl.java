package com.warnes.erp.modules.sales.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.cash.entity.CashMovement;
import com.warnes.erp.modules.cash.entity.CashMovementType;
import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.entity.ShiftStatus;
import com.warnes.erp.modules.cash.repository.CashMovementRepository;
import com.warnes.erp.modules.cash.repository.CashShiftRepository;
import com.warnes.erp.modules.catalog.entity.Product;
import com.warnes.erp.modules.inventory.entity.MovementType;
import com.warnes.erp.modules.inventory.entity.Stock;
import com.warnes.erp.modules.inventory.entity.StockMovement;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.repository.StockRepository;
import com.warnes.erp.modules.inventory.repository.StockMovementRepository;
import com.warnes.erp.modules.inventory.repository.StockSpecification;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.sales.dto.request.CreditNoteRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SaleItemRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CreditNoteResponseDTO;
import com.warnes.erp.modules.sales.entity.*;
import com.warnes.erp.modules.sales.mapper.CreditNoteMapper;
import com.warnes.erp.modules.sales.repository.CreditNoteRepository;
import com.warnes.erp.modules.sales.repository.CustomerRepository;
import com.warnes.erp.modules.sales.repository.SaleRepository;
import com.warnes.erp.modules.sales.service.CreditNoteService;
import com.warnes.erp.modules.sales.service.CustomerLedgerService;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditNoteServiceImpl implements CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final CashShiftRepository cashShiftRepository;
    private final CashMovementRepository cashMovementRepository;
    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    private final CustomerLedgerService customerLedgerService;

    private final CreditNoteMapper creditNoteMapper;

    @Override
    @Transactional
    public void createCreditNote(CreditNoteRequestDTO request) {
        if (creditNoteRepository.existsByCreditNoteNumberIgnoreCase(request.creditNoteNumber())) {
            throw new BusinessRuleException("El número de Nota de Crédito ya existe");
        }

        Sale sale = saleRepository.findByUuid(request.saleUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Venta original no encontrada"));

        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new BusinessRuleException("Solo se pueden generar notas de crédito de ventas completadas");
        }

        User currentUser = getCurrentAuthenticatedUser();
        Warehouse targetWarehouse = warehouseRepository.findByUuid(request.targetWarehouseUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén de destino no encontrado"));

        CreditNote creditNote = new CreditNote();
        creditNote.setCreditNoteNumber(request.creditNoteNumber());
        creditNote.setSale(sale);
        creditNote.setCustomer(sale.getCustomer());
        creditNote.setRefundType(request.refundType());

        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        // 1. Validar ítems, devolver stock y calcular montos
        for (SaleItemRequestDTO returnItem : request.returnItems()) {
            SaleItem originalSaleItem = sale.getItems().stream()
                    .filter(item -> item.getProduct().getUuid().equals(returnItem.productUuid()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException("El producto no pertenece a la venta original"));

            if (returnItem.quantity().compareTo(originalSaleItem.getQuantity()) > 0) {
                throw new BusinessRuleException("No se puede devolver más cantidad de la que se vendió");
            }

            BigDecimal unitPrice = originalSaleItem.getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(returnItem.quantity());
            totalRefundAmount = totalRefundAmount.add(subtotal);

            // Bloqueo pesimista: Ingresar el stock nuevamente a la bodega
            Specification<Stock> filter = StockSpecification.hasWarehouseUuid(targetWarehouse.getUuid())
                    .and(StockSpecification.hasProductUuid(originalSaleItem.getProduct().getUuid()));
            Stock stock = stockRepository.findOne(filter)
                    .orElseThrow(() -> new BusinessRuleException("Stock no inicializado en el almacén de destino"));

            // Cálculo matemático preciso con BigDecimal
            BigDecimal newBalance = stock.getQuantity().add(returnItem.quantity());
            stock.setQuantity(newBalance);
            stockRepository.save(stock);

            // Registro Kardex Inmutable
            recordKardexMovement(originalSaleItem.getProduct(), targetWarehouse, currentUser, returnItem.quantity(), newBalance, MovementType.RETURN_IN, sale.getUuid());

            // Generar ítem de la nota de crédito
            CreditNoteItem cnItem = new CreditNoteItem();
            cnItem.setCreditNote(creditNote);
            cnItem.setProduct(originalSaleItem.getProduct());
            cnItem.setQuantity(returnItem.quantity());
            cnItem.setUnitPrice(unitPrice);
            cnItem.setSubtotal(subtotal);
            creditNote.getItems().add(cnItem);
        }

        creditNote.setTotalAmount(totalRefundAmount);
        creditNoteRepository.save(creditNote);

        // 2. Impacto Financiero
        if (request.refundType() == RefundType.STORE_CREDIT) {
            Customer customer = sale.getCustomer();
            if (customer == null) {
                throw new BusinessRuleException("No se puede generar saldo a favor a un Consumidor Final anónimo");
            }

            // BUG CORREGIDO: Acreditamos el saldo a favor una sola vez
            customer.setCurrentBalance(customer.getCurrentBalance().add(totalRefundAmount));
            customerRepository.save(customer);

            // Registrar el ingreso en el Libro Mayor
            customerLedgerService.recordMovement(
                    customer,
                    sale,
                    LedgerMovementType.STORE_CREDIT_ISSUED,
                    totalRefundAmount,
                    customer.getCurrentBalance()
            );

        } else if (request.refundType() == RefundType.CASH_REFUND) {
            // Devolución física de efectivo
            CashShift activeShift = cashShiftRepository.findByUserIdAndStatus(currentUser.getId(), ShiftStatus.OPEN)
                    .orElseThrow(() -> new BusinessRuleException("Debe tener un turno de caja abierto para devolver efectivo"));

            if (activeShift.getExpectedBalance().compareTo(totalRefundAmount) < 0) {
                throw new BusinessRuleException("No hay suficiente efectivo en caja para realizar la devolución");
            }

            CashMovement refundMovement = new CashMovement();
            refundMovement.setCashShift(activeShift);
            refundMovement.setMovementType(CashMovementType.REFUND);
            refundMovement.setAmount(totalRefundAmount);
            refundMovement.setDescription("Devolución por NC: " + request.creditNoteNumber());
            cashMovementRepository.save(refundMovement);

            activeShift.setExpectedBalance(activeShift.getExpectedBalance().subtract(totalRefundAmount));
            cashShiftRepository.save(activeShift);
        }

        // Si la devolución total iguala a la venta original, se puede marcar la venta como REFUNDED.
        // Omitido por simplicidad, se maneja con reportes sumando las NC vinculadas a una Sale.
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CreditNoteResponseDTO> getCreditNotes(Pageable pageable) {
        return creditNoteRepository.findAll(pageable).map(creditNoteMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditNoteResponseDTO getCreditNoteByUuid(UUID uuid) {
        return creditNoteRepository.findByUuid(uuid)
                .map(creditNoteMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito no encontrada"));
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de auditoría no encontrado"));
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
}