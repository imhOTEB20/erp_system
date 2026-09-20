package com.warnes.erp.modules.sales.repository;

import com.warnes.erp.modules.sales.entity.Customer;
import com.warnes.erp.modules.sales.entity.CustomerLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerLedgerRepository extends JpaRepository<CustomerLedger, Long> {
    // Obtenemos el historial ordenado desde el más reciente al más antiguo
    List<CustomerLedger> findByCustomerOrderByCreatedAtDesc(Customer customer);
}