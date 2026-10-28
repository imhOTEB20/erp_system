package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.reporting.entity.GroupSalesBy;
import com.warnes.erp.modules.reporting.entity.PeriodGranularity;
import com.warnes.erp.modules.sales.dto.request.PayRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SaleCheckoutRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SaleCreateRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SalePaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.response.*;
import com.warnes.erp.modules.sales.entity.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface SaleService {
    SaleResponseDTO createSale(SaleCreateRequestDTO request);
    SaleResponseDTO registerPayments(String invoiceNumber, PayRequestDTO request);
    SaleResponseDTO checkout(SaleCheckoutRequestDTO request);
    SaleResponseDTO getSaleByUuid(UUID uuid);

    Page<SaleResponseDTO> getSales(SaleStatus status, UUID customerUuid, ZonedDateTime from, ZonedDateTime to, Pageable pageable);
    SaleResponseDTO getSaleByInvoiceNumber(String invoiceNumber);
    List<SaleByCategoryResponseDTO> getSaleByCategory(UUID warehouseUuid, ZonedDateTime from, ZonedDateTime to);
    List<SaleByProductResponseDTO> getSaleByProduct(UUID warehouseUuid, Integer limit, ZonedDateTime from, ZonedDateTime to);
    List<SaleByPeriodResponseDTO> getSaleByPeriod(UUID warehouseUuid, PeriodGranularity granularity, ZonedDateTime from, ZonedDateTime to);
    List<ProfitMarginResponseDTO> getProfitMargin(UUID warehouseUuid, GroupSalesBy groupBy, ZonedDateTime from, ZonedDateTime to);
}