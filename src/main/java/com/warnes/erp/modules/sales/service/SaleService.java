package com.warnes.erp.modules.sales.service;

import com.warnes.erp.modules.sales.dto.request.SaleCreateRequestDTO;
import com.warnes.erp.modules.sales.dto.request.SalePaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import java.util.UUID;

public interface SaleService {
    SaleResponseDTO createSale(SaleCreateRequestDTO request);
    SaleResponseDTO registerPayment(UUID saleUuid, SalePaymentRequestDTO request, UUID originWarehouseUuid);
}