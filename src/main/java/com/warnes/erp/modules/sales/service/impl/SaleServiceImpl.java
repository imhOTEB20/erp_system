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
import com.warnes.erp.modules.catalog.repository.ProductRepository;
import com.warnes.erp.modules.inventory.entity.MovementType;
import com.warnes.erp.modules.inventory.entity.Stock;
import com.warnes.erp.modules.inventory.entity.StockMovement;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.repository.StockRepository;
import com.warnes.erp.modules.inventory.repository.StockMovementRepository;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import com.warnes.erp.modules.sales.dto.request.SaleCreateRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SaleItemRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SalePaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.entity.*;
import com.warnes.erp.modules.sales.mapper.SaleMapper;
import com.warnes.erp.modules.sales.repository.CustomerRepository;
import com.warnes.erp.modules.sales.repository.SaleRepository;
import com.warnes.erp.modules.sales.service.CustomerLedgerService;
import com.warnes.erp.modules.sales.service.SaleService;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final CashShiftRepository cashShiftRepository;
    private final CashMovementRepository cashMovementRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SaleMapper saleMapper;

    private final CustomerLedgerService customerLedgerService;

    @Override
    @Transactional
    public SaleResponseDTO createSale(SaleCreateRequestDTO request) {
        if (saleRepository.existsByInvoiceNumberIgnoreCase(request.invoiceNumber())) {
            throw new BusinessRuleException("El número de factura ya existe");
        }

        User currentUser = getCurrentAuthenticatedUser();
        CashShift activeShift = getActiveCashShift(currentUser);
        Warehouse originWarehouse = getWarehouse(request.originWarehouseUuid());

        Customer customer = null;
        BigDecimal customerMargin = BigDecimal.ZERO;
        if (request.customerUuid() != null) {
            customer = customerRepository.findByUuid(request.customerUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            if (customer.getPriceList() != null) {
                customerMargin = customer.getPriceList().getMarginPercentage();
            }
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber(request.invoiceNumber());
        sale.setCashShift(activeShift);
        sale.setCustomer(customer);
        sale.setStatus(SaleStatus.PENDING_PAYMENT); // La venta nace pendiente

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequestDTO itemDto : request.items()) {
            Product product = productRepository.findByUuid(itemDto.productUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // Solo verificamos disponibilidad de forma optimista, el bloqueo fuerte ocurre al pagar
            Stock stock = stockRepository.findByProductIdAndWarehouseId(product.getId(), originWarehouse.getId())
                    .orElseThrow(() -> new BusinessRuleException("Stock no inicializado para: " + product.getSku()));

            // Corrección: Usar getQuantity() y comparar con BigDecimal
            if (stock.getQuantity().compareTo(itemDto.quantity()) < 0) {
                throw new BusinessRuleException("Stock insuficiente previo de " + product.getSku());
            }

            BigDecimal finalUnitPrice = calculateFinalPrice(product.getSalePrice(), customerMargin);
            BigDecimal subtotal = finalUnitPrice.multiply(itemDto.quantity());
            totalAmount = totalAmount.add(subtotal);

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemDto.quantity());
            saleItem.setUnitPrice(finalUnitPrice);
            saleItem.setSubtotal(subtotal);
            sale.getItems().add(saleItem);
        }

        sale.setTotalAmount(totalAmount);
        return saleMapper.toResponseDTO(saleRepository.save(sale));
    }

    @Override
    @Transactional
    public SaleResponseDTO registerPayment(UUID saleUuid, SalePaymentRequestDTO request, UUID originWarehouseUuid) {
        Sale sale = saleRepository.findByUuid(saleUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        if (sale.getStatus() != SaleStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("La venta no está pendiente de pago");
        }

        // Lógica: Pago con Saldo a Favor o A Crédito (Deuda)
        if (request.paymentMethod() == PaymentMethod.STORE_CREDIT || request.paymentMethod() == PaymentMethod.ON_CREDIT) {
            if (sale.getCustomer() == null) {
                throw new BusinessRuleException("No se puede operar con cuentas corrientes en ventas a Consumidor Final");
            }

            // Bloqueo pesimista del cliente para evitar condiciones de carrera
            Customer customer = customerRepository.findByIdForUpdate(sale.getCustomer().getId())
                    .orElseThrow(() -> new BusinessRuleException("Error de concurrencia con el cliente"));

            BigDecimal projectedBalance = customer.getCurrentBalance().subtract(request.amount());

            if (request.paymentMethod() == PaymentMethod.STORE_CREDIT) {
                // Solo puede usar la plata que tiene a favor (no puede bajar de 0 con este método)
                if (projectedBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessRuleException("Saldo a favor insuficiente. Disponible: $" + customer.getCurrentBalance());
                }
            } else { // ON_CREDIT
                // Puede bajar de 0, pero sin superar el límite de crédito (negativo)
                BigDecimal maxAllowedDebt = customer.getCreditLimit().negate();
                if (projectedBalance.compareTo(maxAllowedDebt) < 0) {
                    throw new BusinessRuleException(
                            "Límite de crédito excedido. Límite: $" + customer.getCreditLimit() +
                                    " | Deuda actual: $" + customer.getCurrentBalance().min(BigDecimal.ZERO).abs()
                    );
                }
            }

            // Deducir saldo (o aumentar deuda)
            customer.setCurrentBalance(projectedBalance);
            customerRepository.save(customer);

            // Registrar en el Libro Mayor
            LedgerMovementType ledgerType = request.paymentMethod() == PaymentMethod.STORE_CREDIT
                    ? LedgerMovementType.PAYMENT_WITH_CREDIT
                    : LedgerMovementType.SALE_ON_CREDIT;

            customerLedgerService.recordMovement(customer, sale, ledgerType, request.amount(), customer.getCurrentBalance());
        }

        SalePayment payment = new SalePayment();
        payment.setSale(sale);
        payment.setPaymentMethod(request.paymentMethod());
        payment.setAmount(request.amount());
        sale.getPayments().add(payment);

        BigDecimal totalPaid = sale.getPayments().stream()
                .map(SalePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ajuste de Centavos (Tolerancia de $10 pesos argentinos)
        BigDecimal difference = sale.getTotalAmount().subtract(totalPaid);
        if (difference.compareTo(BigDecimal.ZERO) > 0 && difference.compareTo(new BigDecimal("10.00")) <= 0) {
            SalePayment roundingAdjustment = new SalePayment();
            roundingAdjustment.setSale(sale);
            roundingAdjustment.setPaymentMethod(PaymentMethod.ROUNDING_ADJUSTMENT);
            roundingAdjustment.setAmount(difference);
            sale.getPayments().add(roundingAdjustment);
            totalPaid = totalPaid.add(difference);
        }

        if (totalPaid.compareTo(sale.getTotalAmount()) >= 0) {
            completeSaleTransaction(sale, originWarehouseUuid);
        }

        return saleMapper.toResponseDTO(saleRepository.save(sale));
    }

    private void completeSaleTransaction(Sale sale, UUID originWarehouseUuid) {
        Warehouse originWarehouse = getWarehouse(originWarehouseUuid);
        User currentUser = getCurrentAuthenticatedUser();

        // 1. Bloqueo Pesimista y deducción de Stock
        for (SaleItem item : sale.getItems()) {
            Stock stock = stockRepository.findByProductAndWarehouseForUpdate(item.getProduct().getId(), originWarehouse.getId())
                    .orElseThrow(() -> new BusinessRuleException("Stock no inicializado"));

            // Corrección: Deducción matemática precisa con BigDecimal
            BigDecimal newBalance = stock.getQuantity().subtract(item.getQuantity());

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException("Stock insuficiente durante el cobro para: " + item.getProduct().getSku());
            }

            // Actualizamos la tabla de acceso rápido
            stock.setQuantity(newBalance);
            stockRepository.save(stock);

            // Corrección: Invocamos al Kardex inmutable con los 6 parámetros requeridos (incluyendo balanceAfter y UUID)
            recordKardexMovement(item.getProduct(), originWarehouse, currentUser, item.getQuantity(), newBalance, sale.getUuid());
        }

        // 2. Impactar los pagos en la Caja Activa
        CashShift shift = sale.getCashShift();
        for (SalePayment payment : sale.getPayments()) {
            CashMovement cashMovement = new CashMovement();
            cashMovement.setCashShift(shift);
            cashMovement.setMovementType(CashMovementType.SALE_PAYMENT);
            cashMovement.setAmount(payment.getAmount());
            cashMovement.setDescription("Pago Factura: " + sale.getInvoiceNumber() + " [" + payment.getPaymentMethod().name() + "]");
            cashMovementRepository.save(cashMovement);

            // Solo sumar al balance esperado si es EFECTIVO (para facilitar el arqueo de gaveta física)
            if (payment.getPaymentMethod() == PaymentMethod.CASH) {
                shift.setExpectedBalance(shift.getExpectedBalance().add(payment.getAmount()));
            }
        }

        cashShiftRepository.save(shift);
        sale.setStatus(SaleStatus.COMPLETED);
    }

    private void recordKardexMovement(Product product, Warehouse warehouse, User user, BigDecimal quantity, BigDecimal balanceAfter, UUID saleUuid) {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse); // Cambiado de originWarehouse
        movement.setUser(user);
        movement.setMovementType(MovementType.SALE); // Mantenemos tu Enum intacto

        // Conversión a BigDecimal para cumplir con el nuevo esquema
        movement.setQuantity(quantity);

        // Reglas de Auditoría Inmutable
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceDocument(saleUuid);

        stockMovementRepository.save(movement);
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de auditoría no encontrado"));
    }

    private CashShift getActiveCashShift(User user) {
        // Requiere agregar: Optional<CashShift> findByUserIdAndStatus(Long userId, ShiftStatus status); en CashShiftRepository
        return cashShiftRepository.findByUserIdAndStatus(user.getId(), ShiftStatus.OPEN)
                .orElseThrow(() -> new BusinessRuleException("El usuario no posee un turno de caja abierto actualmente"));
    }

    private Warehouse getWarehouse(UUID warehouseUuid) {
        return warehouseRepository.findByUuid(warehouseUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado"));
    }

    private BigDecimal calculateFinalPrice(BigDecimal basePrice, BigDecimal marginPercentage) {
        if (marginPercentage == null || marginPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return basePrice;
        }
        // Calcula: precioBase * (1 + (margen / 100))
        BigDecimal multiplier = BigDecimal.ONE.add(marginPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleByUuid(UUID uuid) {
        Sale sale = saleRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));
        return saleMapper.toResponseDTO(sale);
    }
}