package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.request.SaleCheckoutRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SaleCreateRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SalePaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface SaleService {
    SaleResponseDTO createSale(SaleCreateRequestDTO request);
    SaleResponseDTO registerPayments(UUID saleUuid, List<SalePaymentRequestDTO> requests, UUID originWarehouseUuid);
    SaleResponseDTO checkout(SaleCheckoutRequestDTO request);
    SaleResponseDTO getSaleByUuid(UUID uuid);

    Page<SaleResponseDTO> getSales(SaleStatus status, UUID customerUuid, ZonedDateTime from, ZonedDateTime to, Pageable pageable);
    SaleResponseDTO getSaleByInvoiceNumber(String invoiceNumber);
}