package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.request.CustomerPaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.request.CustomerRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CustomerResponseDTO;
import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerResponseDTO createCustomer(CustomerRequestDTO request);
    CustomerResponseDTO getCustomerByUuid(UUID uuid);
    List<CustomerResponseDTO> getAllCustomers();
    CustomerResponseDTO updateCustomer(UUID uuid, CustomerRequestDTO request);
    void deleteCustomer(UUID uuid);
    void registerDebtPayment(UUID customerUuid, CustomerPaymentRequestDTO request);
}