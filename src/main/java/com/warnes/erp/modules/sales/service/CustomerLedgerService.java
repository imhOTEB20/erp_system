package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.response.CustomerLedgerResponseDTO;
import com.warnes.erp.modules.sales.entity.Customer;
import com.warnes.erp.modules.sales.entity.LedgerMovementType;
import com.warnes.erp.modules.sales.entity.Sale;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CustomerLedgerService {
    // Método interno para usar dentro de @Transactional en otros servicios
    void recordMovement(Customer customer, Sale sale, LedgerMovementType type, BigDecimal amount, BigDecimal balanceAfter);

    // Método expuesto para el controlador REST
    List<CustomerLedgerResponseDTO> getLedgerByCustomerUuid(UUID customerUuid);
}