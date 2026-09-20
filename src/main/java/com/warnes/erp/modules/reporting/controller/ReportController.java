package com.warnes.erp.modules.reporting.controller;

import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;
import com.warnes.erp.modules.inventory.service.StockService;
import com.warnes.erp.modules.reporting.service.PdfService;
import com.warnes.erp.modules.sales.dto.response.SaleResponseDTO;
import com.warnes.erp.modules.sales.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final PdfService pdfService;
    private final SaleService saleService;
    private final StockService stockService;

    @GetMapping("/sales/{uuid}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<byte[]> downloadInvoiceA4(@PathVariable UUID uuid) {
        // Reutilizamos el DTO existente que armaste en SaleServiceImpl
        SaleResponseDTO sale = saleService.getSaleByUuid(uuid);

        Map<String, Object> data = new HashMap<>();
        data.put("sale", sale);
        data.put("companyName", "Repuestos Warnes S.A.");
        data.put("printDate", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        byte[] pdfBytes = pdfService.generatePdfFromHtml("invoice-a4", data);

        return buildPdfResponse(pdfBytes, "Factura_" + sale.invoiceNumber() + ".pdf");
    }

    @GetMapping("/sales/{uuid}/ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<byte[]> downloadTicket80mm(@PathVariable UUID uuid) {
        SaleResponseDTO sale = saleService.getSaleByUuid(uuid);

        Map<String, Object> data = new HashMap<>();
        data.put("sale", sale);

        byte[] pdfBytes = pdfService.generatePdfFromHtml("ticket-80mm", data);

        return buildPdfResponse(pdfBytes, "Ticket_" + sale.invoiceNumber() + ".pdf");
    }

    @GetMapping("/inventory/{productUuid}/kardex")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<byte[]> downloadKardexReport(@PathVariable UUID productUuid) {
        List<StockMovementResponseDTO> movements = stockService.getProductKardex(productUuid);

        Map<String, Object> data = new HashMap<>();
        data.put("movements", movements);
        data.put("reportDate", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        byte[] pdfBytes = pdfService.generatePdfFromHtml("kardex-report", data);

        return buildPdfResponse(pdfBytes, "Kardex_Producto.pdf");
    }

    /**
     * Utilidad para inyectar los headers HTTP correctos para la descarga de PDFs
     */
    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}