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
import com.warnes.erp.modules.sales.dto.request.*;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.entity.*;
import com.warnes.erp.modules.sales.mapper.SaleMapper;
import com.warnes.erp.modules.sales.repository.CustomerRepository;
import com.warnes.erp.modules.sales.repository.SaleRepository;
import com.warnes.erp.modules.sales.repository.SaleSpecification;
import com.warnes.erp.modules.sales.service.CustomerLedgerService;
import com.warnes.erp.modules.sales.service.InvoiceGeneratorService;
import com.warnes.erp.modules.sales.service.SaleService;
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
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
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
    private final InvoiceGeneratorService invoiceGeneratorService;

    @Override
    @Transactional // Garantiza ACID en la facturación y la validación de inventario
    public SaleResponseDTO createSale(SaleCreateRequestDTO request) {

        // 1. Trazabilidad de Auditoría (El "Fantasma")
        // Obtenemos al usuario autenticado desde el SecurityContext de Spring (Token JWT)
        // Nunca confiamos en un ID enviado desde el frontend
        User currentUser = getCurrentAuthenticatedUser();

        Sale sale = new Sale();
        String generatedInvoiceNumber = invoiceGeneratorService.generateSaleInvoiceNumber(request.isB2B());
        sale.setInvoiceNumber(generatedInvoiceNumber);
        sale.setUser(currentUser); // Firma inmutable del responsable de la operación
        sale.setStatus(SaleStatus.PENDING_PAYMENT); // Reserva lógica, espera registro de pago

        // 2. Control B2B vs POS
        if (request.isB2B()) {
            if (request.customerUuid() == null) {
                throw new BusinessRuleException("Las ventas B2B por Cuenta Corriente exigen un cliente registrado.");
            }
            // Al ser operación administrativa/mayorista, se desvincula de las cajas físicas
            sale.setCashShift(null);
        } else {
            // Operación de mostrador minorista. Obliga a que el cajero tenga su turno activo.
            CashShift activeShift = getActiveCashShift(currentUser);
            sale.setCashShift(activeShift);
        }

        // 3. Resolución de Lista de Precios y Cliente
        BigDecimal customerMargin = BigDecimal.ZERO;
        if (request.customerUuid() != null) {
            Customer customer = customerRepository.findByUuid(request.customerUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

            sale.setCustomer(customer);

            if (customer.getPriceList() != null) {
                customerMargin = customer.getPriceList().getMarginPercentage();
            }
        }

        // 4. Bloqueo PESSIMISTIC_WRITE de Inventario e Inserción de Items
        Warehouse originWarehouse = getWarehouse(request.originWarehouseUuid());
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequestDTO itemDto : request.items()) {
            Product product = productRepository.findByUuid(itemDto.productUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // Bloqueo a nivel de base de datos para evitar Race Conditions (Double-Spend) de Stock
            Stock stock = stockRepository.findByProductAndWarehouseForUpdate(product.getId(), originWarehouse.getId())
                    .orElseThrow(() -> new BusinessRuleException("Stock no inicializado en el almacén origen para: " + product.getSku()));

            // La deducción final ocurre al pagar, pero validamos la reserva temprana
            if (stock.getQuantity().compareTo(itemDto.quantity()) < 0) {
                throw new BusinessRuleException(
                        "Stock insuficiente previo de " + product.getSku() + ". Disponible: " + stock.getQuantity()
                );
            }

            // Cálculo dinámico de márgenes y descuentos redondeado a 2 decimales para AFIP
            BigDecimal basePrice = product.getSalePrice();
            BigDecimal multiplier = BigDecimal.ONE.add(customerMargin.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            BigDecimal finalUnitPrice = basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = finalUnitPrice.multiply(itemDto.quantity()).setScale(2, RoundingMode.HALF_UP);

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemDto.quantity());
            saleItem.setUnitPrice(finalUnitPrice);
            saleItem.setSubtotal(subtotal);

            sale.getItems().add(saleItem);
            totalAmount = totalAmount.add(subtotal);
        }

        sale.setTotalAmount(totalAmount);

        // Guardado en cascada de cabecera y detalle
        Sale savedSale = saleRepository.save(sale);

        return saleMapper.toResponseDTO(savedSale);
    }

    @Override
    @Transactional
    public SaleResponseDTO registerPayments(String invoiceNumber, PayRequestDTO request) {
        Sale sale = saleRepository.findByInvoiceNumberIgnoreCase(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        if (sale.getStatus() != SaleStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("La venta ya no se encuentra pendiente de pago");
        }

        // Iterar sobre cada método de pago enviado (Ej: 40% Efectivo + 60% Transferencia)
        for (SalePaymentRequestDTO paymentRequestDTO : request.payments()) {

            // Validaciones de Cuenta Corriente (Deuda o Saldo a favor)
            if (paymentRequestDTO.paymentMethod() == PaymentMethod.STORE_CREDIT || paymentRequestDTO.paymentMethod() == PaymentMethod.ON_CREDIT) {
                if (sale.getCustomer() == null) {
                    throw new BusinessRuleException("No se puede operar con cuentas corrientes en ventas a Consumidor Final");
                }

                Customer customer = customerRepository.findByIdForUpdate(sale.getCustomer().getId())
                        .orElseThrow(() -> new BusinessRuleException("Error de concurrencia con el cliente"));

                BigDecimal projectedBalance = customer.getCurrentBalance().subtract(paymentRequestDTO.amount());

                if (paymentRequestDTO.paymentMethod() == PaymentMethod.STORE_CREDIT && projectedBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessRuleException("Saldo a favor insuficiente. Disponible: $" + customer.getCurrentBalance());
                } else if (paymentRequestDTO.paymentMethod() == PaymentMethod.ON_CREDIT) {
                    BigDecimal maxAllowedDebt = customer.getCreditLimit().negate();
                    if (projectedBalance.compareTo(maxAllowedDebt) < 0) {
                        throw new BusinessRuleException("Límite de crédito excedido.");
                    }
                }

                customer.setCurrentBalance(projectedBalance);
                customerRepository.save(customer);

                LedgerMovementType ledgerType = paymentRequestDTO.paymentMethod() == PaymentMethod.STORE_CREDIT
                        ? LedgerMovementType.PAYMENT_WITH_CREDIT
                        : LedgerMovementType.SALE_ON_CREDIT;
                customerLedgerService.recordMovement(customer, sale, ledgerType, paymentRequestDTO.amount(), customer.getCurrentBalance());
            }

            SalePayment payment = new SalePayment();
            payment.setSale(sale);
            payment.setPaymentMethod(paymentRequestDTO.paymentMethod());
            payment.setAmount(paymentRequestDTO.amount());
            sale.getPayments().add(payment);
        }

        BigDecimal totalPaid = sale.getPayments().stream()
                .map(SalePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tolerancia de redondeo ($200 ARS)
        BigDecimal difference = sale.getTotalAmount().subtract(totalPaid);
        if (difference.compareTo(BigDecimal.ZERO) > 0 && difference.compareTo(new BigDecimal("200.00")) <= 0) {
            SalePayment roundingAdjustment = new SalePayment();
            roundingAdjustment.setSale(sale);
            roundingAdjustment.setPaymentMethod(PaymentMethod.ROUNDING_ADJUSTMENT);
            roundingAdjustment.setAmount(difference);
            sale.getPayments().add(roundingAdjustment);
            totalPaid = totalPaid.add(difference);
        }

        // Consolidación final si se cubrió el total
        if (totalPaid.compareTo(sale.getTotalAmount()) >= 0) {
            // Deducción de sucursal: Toma la de la caja si es POS, o la que vino por parámetro si es B2B
            UUID effectiveWarehouseUuid = sale.getCashShift() != null
                    ? sale.getCashShift().getCashRegister().getWarehouse().getUuid()
                    : request.warehouseUuid();

            if (effectiveWarehouseUuid == null) {
                throw new BusinessRuleException("No se pudo determinar el almacén para deducir el stock (B2B exige originWarehouseUuid).");
            }
            completeSaleTransaction(sale, effectiveWarehouseUuid);
        }

        return saleMapper.toResponseDTO(saleRepository.save(sale));
    }

    @Override
    @Transactional // Garantiza que toda la operatoria sea atómica (ACID)
    public SaleResponseDTO checkout(SaleCheckoutRequestDTO request) {
        User currentUser = getCurrentAuthenticatedUser();

        // 1. RESOLUCIÓN DE CONTEXTO Y SUCURSAL (B2B vs POS)
        CashShift activeShift = getActiveCashShift(currentUser);
        Warehouse originWarehouse = activeShift.getCashRegister().getWarehouse();

        // 2. RESOLUCIÓN DE CLIENTE Y MÁRGENES COMERCIALES
        Customer customer = null;
        BigDecimal customerMargin = BigDecimal.ZERO;
        if (request.customerUuid() != null) {
            customer = customerRepository.findByUuid(request.customerUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));
            if (customer.getPriceList() != null) {
                customerMargin = customer.getPriceList().getMarginPercentage();
            }
        }

        // 3. INICIALIZACIÓN DE LA FACTURA (Con Secuencias Nativas AFIP)
        Sale sale = new Sale();
        sale.setInvoiceNumber(invoiceGeneratorService.generateSaleInvoiceNumber(false));
        sale.setUser(currentUser);
        sale.setCashShift(activeShift);
        sale.setCustomer(customer);
        sale.setStatus(SaleStatus.COMPLETED); // Nace completada al ser un One-Step Checkout

        // Persistimos la cabecera tempranamente para obtener el ID/UUID requerido en las FK del Kardex y Pagos
        sale = saleRepository.save(sale);

        // 4. PROCESAMIENTO DE ITEMS, BLOQUEO PESIMISTA Y KARDEX
        BigDecimal saleTotalAmount = BigDecimal.ZERO;

        for (SaleItemRequestDTO itemDto : request.items()) {
            Product product = productRepository.findByUuid(itemDto.productUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));

            // Lock Pesimista (FOR UPDATE) en BD: Evita la condición de carrera si 2 cajeros venden la misma bujía al mismo milisegundo
            Stock stock = stockRepository.findByProductAndWarehouseForUpdate(product.getId(), originWarehouse.getId())
                    .orElseThrow(() -> new BusinessRuleException("Stock no inicializado para: " + product.getSku()));

            if (stock.getQuantity().compareTo(itemDto.quantity()) < 0) {
                throw new BusinessRuleException("Stock insuficiente para: " + product.getSku() + ". Disponible: " + stock.getQuantity());
            }

            // Deducción inmutable de inventario
            BigDecimal newBalance = stock.getQuantity().subtract(itemDto.quantity());
            stock.setQuantity(newBalance);
            stockRepository.save(stock);

            // Registro inmutable de auditoría en Kardex
            recordKardexMovement(product, originWarehouse, currentUser, itemDto.quantity(), newBalance, sale.getUuid());

            // Cálculos financieros exactos
            BigDecimal finalUnitPrice = calculateFinalPrice(product.getSalePrice(), customerMargin);
            BigDecimal subtotal = finalUnitPrice.multiply(itemDto.quantity()).setScale(2, RoundingMode.HALF_UP);

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemDto.quantity());
            saleItem.setUnitPrice(finalUnitPrice);
            saleItem.setSubtotal(subtotal);
            sale.getItems().add(saleItem);

            saleTotalAmount = saleTotalAmount.add(subtotal);
        }
        sale.setTotalAmount(saleTotalAmount);

        // 5. PROCESAMIENTO DE PAGOS MIXTOS
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (SalePaymentRequestDTO paymentDto : request.payments()) {
            PaymentMethod method = paymentDto.paymentMethod();
            BigDecimal amount = paymentDto.amount();

            // Lógica Financiera: Cuentas Corrientes y Límite de Crédito
            if (method == PaymentMethod.STORE_CREDIT || method == PaymentMethod.ON_CREDIT) {
                if (customer == null) {
                    throw new BusinessRuleException("No se puede operar con cuenta corriente a Consumidor Final.");
                }

                // Lock Pesimista del cliente
                Customer lockedCustomer = customerRepository.findByIdForUpdate(customer.getId())
                        .orElseThrow(() -> new BusinessRuleException("Error de concurrencia al procesar el cliente."));

                BigDecimal projectedBalance = lockedCustomer.getCurrentBalance().subtract(amount);

                if (method == PaymentMethod.STORE_CREDIT && projectedBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessRuleException("Saldo a favor insuficiente. Disponible: $" + lockedCustomer.getCurrentBalance());
                } else if (method == PaymentMethod.ON_CREDIT) {
                    BigDecimal maxAllowedDebt = lockedCustomer.getCreditLimit().negate();
                    if (projectedBalance.compareTo(maxAllowedDebt) < 0) {
                        throw new BusinessRuleException("Límite de crédito excedido. Disponible: $" + lockedCustomer.getCurrentBalance().subtract(maxAllowedDebt));
                    }
                }

                lockedCustomer.setCurrentBalance(projectedBalance);
                customerRepository.save(lockedCustomer);

                // Asentar en Libro Mayor
                LedgerMovementType ledgerType = method == PaymentMethod.STORE_CREDIT ? LedgerMovementType.PAYMENT_WITH_CREDIT : LedgerMovementType.SALE_ON_CREDIT;
                customerLedgerService.recordMovement(lockedCustomer, sale, ledgerType, amount, lockedCustomer.getCurrentBalance());
            }

            // Lógica Física: Arqueo de Caja (Solo el billete físico altera la gaveta)
            if (method == PaymentMethod.CASH) {
                CashMovement cashMovement = new CashMovement();
                cashMovement.setCashShift(activeShift);
                cashMovement.setMovementType(CashMovementType.SALE_PAYMENT);
                cashMovement.setAmount(amount);
                cashMovement.setDescription("Pago Factura: " + sale.getInvoiceNumber());
                cashMovement.setUser(currentUser);
                cashMovementRepository.save(cashMovement);

                // Impactar el balance esperado a rendir
                activeShift.setExpectedBalance(activeShift.getExpectedBalance().add(amount));
                cashShiftRepository.save(activeShift);
            }

            SalePayment payment = new SalePayment();
            payment.setSale(sale);
            payment.setPaymentMethod(method);
            payment.setAmount(amount);
            sale.getPayments().add(payment);

            totalPaid = totalPaid.add(amount);
        }

        // 6. CONCILIACIÓN MATEMÁTICA Y REDONDEO
        BigDecimal difference = saleTotalAmount.subtract(totalPaid);

        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            // Tolerancia contable de hasta $200.00 ARS en mostrador
            if (difference.compareTo(new BigDecimal("200.00")) <= 0) {
                SalePayment roundingAdjustment = new SalePayment();
                roundingAdjustment.setSale(sale);
                roundingAdjustment.setPaymentMethod(PaymentMethod.ROUNDING_ADJUSTMENT);
                roundingAdjustment.setAmount(difference);
                sale.getPayments().add(roundingAdjustment);
                totalPaid = totalPaid.add(difference);
            } else {
                throw new BusinessRuleException("Fondos insuficientes: El abono total ($" + totalPaid + ") no cubre el monto de la factura ($" + saleTotalAmount + ").");
            }
        } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Error contable: El pago ($" + totalPaid + ") excede el total a facturar ($" + saleTotalAmount + "). Ingrese el cambio correcto.");
        }

        // Guardamos el detalle completo y retornamos el DTO limpio (sin exponer IDs)
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
        if (shift != null) {
            for (SalePayment payment : sale.getPayments()) {
                // Solo registramos el movimiento transaccional en la caja si implica flujo de dinero (ej. ignorar ON_CREDIT)
                if (payment.getPaymentMethod() != PaymentMethod.ON_CREDIT) {
                    CashMovement cashMovement = new CashMovement();
                    cashMovement.setCashShift(shift);
                    cashMovement.setMovementType(CashMovementType.SALE_PAYMENT);
                    // El cajero solo ve el ingreso de capital (amount). El recargo va directo al banco.
                    cashMovement.setAmount(payment.getAmount());
                    cashMovement.setDescription("Pago Factura: " + sale.getInvoiceNumber() + " [" + payment.getPaymentMethod().name() + "]");
                    // NUEVO: Trazabilidad de quién cobró
                    cashMovement.setUser(currentUser);
                    cashMovementRepository.save(cashMovement);

                    // CORRECCIÓN VITAL: Solo el EFECTIVO suma al expectedBalance (gaveta física)
                    if (payment.getPaymentMethod() == PaymentMethod.CASH) {
                        shift.setExpectedBalance(shift.getExpectedBalance().add(payment.getAmount()));
                    }
                }
            }

            cashShiftRepository.save(shift);
            sale.setStatus(SaleStatus.COMPLETED);
        }
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

    @Override
    @Transactional(readOnly = true)
    public Page<SaleResponseDTO> getSales(SaleStatus status, UUID customerUuid, ZonedDateTime from, ZonedDateTime to, Pageable pageable) {
        Specification<Sale> spec = SaleSpecification.withDynamicFilters(status, customerUuid, from, to);
        return saleRepository.findAll(spec, pageable).map(saleMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleByInvoiceNumber(String invoiceNumber) {
        return saleRepository.findByInvoiceNumberIgnoreCase(invoiceNumber)
                .map(saleMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró factura con el número proporcionado"));
    }
}