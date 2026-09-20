package com.warnes.erp.modules.sales.service.impl;

import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.sales.dto.response.CustomerLedgerResponseDTO;
import com.warnes.erp.modules.sales.entity.Customer;
import com.warnes.erp.modules.sales.entity.CustomerLedger;
import com.warnes.erp.modules.sales.entity.LedgerMovementType;
import com.warnes.erp.modules.sales.entity.Sale;
import com.warnes.erp.modules.sales.mapper.CustomerLedgerMapper;
import com.warnes.erp.modules.sales.repository.CustomerLedgerRepository;
import com.warnes.erp.modules.sales.repository.CustomerRepository;
import com.warnes.erp.modules.sales.service.CustomerLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerLedgerServiceImpl implements CustomerLedgerService {

    private final CustomerLedgerRepository ledgerRepository;
    private final CustomerRepository customerRepository;
    private final CustomerLedgerMapper ledgerMapper;

    // Propagation.MANDATORY asegura que este método SOLO pueda ser llamado
    // si ya existe una transacción activa (ej. desde SaleServiceImpl o CreditNoteServiceImpl)
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordMovement(Customer customer, Sale sale, LedgerMovementType type, BigDecimal amount, BigDecimal balanceAfter) {
        CustomerLedger ledger = new CustomerLedger();
        ledger.setCustomer(customer);
        ledger.setSale(sale);
        ledger.setMovementType(type);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(balanceAfter);

        ledgerRepository.save(ledger);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerLedgerResponseDTO> getLedgerByCustomerUuid(UUID customerUuid) {
        Customer customer = customerRepository.findByUuid(customerUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return ledgerRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(ledgerMapper::toResponseDTO)
                .toList();
    }
}