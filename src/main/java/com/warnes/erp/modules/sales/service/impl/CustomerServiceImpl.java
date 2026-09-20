package com.warnes.erp.modules.sales.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ConcurrencyConflictException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.cash.entity.CashMovement;
import com.warnes.erp.modules.cash.entity.CashMovementType;
import com.warnes.erp.modules.cash.entity.CashShift;
import com.warnes.erp.modules.cash.repository.CashMovementRepository;
import com.warnes.erp.modules.cash.repository.CashShiftRepository;
import com.warnes.erp.modules.sales.dto.request.CustomerPaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.request.CustomerRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CustomerResponseDTO;
import com.warnes.erp.modules.sales.entity.Customer;
import com.warnes.erp.modules.sales.entity.LedgerMovementType;
import com.warnes.erp.modules.sales.entity.PaymentMethod;
import com.warnes.erp.modules.sales.entity.PriceList;
import com.warnes.erp.modules.sales.mapper.CustomerMapper;
import com.warnes.erp.modules.sales.repository.CustomerRepository;
import com.warnes.erp.modules.sales.repository.PriceListRepository;
import com.warnes.erp.modules.sales.service.CustomerLedgerService;
import com.warnes.erp.modules.sales.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PriceListRepository priceListRepository; // Repositorio JPA estándar para PriceList
    private final CustomerMapper customerMapper;
    private final CashShiftRepository cashShiftRepository;
    private final CashMovementRepository cashMovementRepository;

    private final CustomerLedgerService customerLedgerService;

    @Override
    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {
        if (customerRepository.existsByDocNumber(request.docNumber())) {
            throw new BusinessRuleException("Ya existe un cliente con este documento");
        }

        Customer customer = customerMapper.toEntity(request);
        assignPriceListIfPresent(request.priceListUuid(), customer);

        return customerMapper.toResponseDTO(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerByUuid(UUID uuid) {
        return customerMapper.toResponseDTO(findEntityByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public CustomerResponseDTO updateCustomer(UUID uuid, CustomerRequestDTO request) {
        Customer customer = findEntityByUuid(uuid);

        if (!customer.getDocNumber().equals(request.docNumber()) &&
                customerRepository.existsByDocNumber(request.docNumber())) {
            throw new BusinessRuleException("El nuevo documento ya está en uso por otro cliente");
        }

        customerMapper.updateEntityFromDto(request, customer);
        assignPriceListIfPresent(request.priceListUuid(), customer);

        return customerMapper.toResponseDTO(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID uuid) {
        Customer customer = findEntityByUuid(uuid);
        customer.setDeletedAt(ZonedDateTime.now());
        customerRepository.save(customer);
    }

    @Transactional
    public void registerDebtPayment(UUID customerUuid, CustomerPaymentRequestDTO request) {
        // Validación estricta
        if (request.paymentMethod() == PaymentMethod.STORE_CREDIT || request.paymentMethod() == PaymentMethod.ON_CREDIT) {
            throw new BusinessRuleException("Método de pago inválido para saldar deudas.");
        }

        Customer customer = customerRepository.findByIdForUpdate(customerRepository.findByUuid(customerUuid)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado")).getId())
                .orElseThrow(() -> new ConcurrencyConflictException("Cliente bloqueado."));

        // Impactar saldo (El pago SUMA al balance, reduciendo la deuda o generando saldo a favor)
        customer.setCurrentBalance(customer.getCurrentBalance().add(request.amount()));
        customerRepository.save(customer);

        // Registro en Libro Mayor
        customerLedgerService.recordMovement(customer, null, LedgerMovementType.DEBT_PAYMENT, request.amount(), customer.getCurrentBalance());

        // Impacto en Caja (Si paga en efectivo físico, debe sumar a la gaveta del cajero)
        if (request.paymentMethod() == PaymentMethod.CASH) {
            if (request.cashShiftUuid() == null) {
                throw new BusinessRuleException("Se requiere el ID del turno de caja para pagos en efectivo.");
            }
            CashShift shift = cashShiftRepository.findByUuid(request.cashShiftUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno de caja no encontrado"));

            // ... validar que el turno esté abierto ...

            CashMovement collectionMovement = new CashMovement();
            collectionMovement.setCashShift(shift);
            collectionMovement.setMovementType(CashMovementType.INCOME);
            collectionMovement.setAmount(request.amount());
            collectionMovement.setDescription("Cobranza Cuenta Corriente - Cliente: " + customer.getDocNumber());
            cashMovementRepository.save(collectionMovement);

            shift.setExpectedBalance(shift.getExpectedBalance().add(request.amount()));
            cashShiftRepository.save(shift);
        }
        // Nota: Si el pago es TRANSFER o CREDIT_CARD, el dinero va al banco, no impacta gaveta.
    }

    private Customer findEntityByUuid(UUID uuid) {
        return customerRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private void assignPriceListIfPresent(UUID priceListUuid, Customer customer) {
        if (priceListUuid != null) {
            PriceList priceList = priceListRepository.findByUuid(priceListUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada"));
            customer.setPriceList(priceList);
        } else {
            customer.setPriceList(null);
        }
    }
}