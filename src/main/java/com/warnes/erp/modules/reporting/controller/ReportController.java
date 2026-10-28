package com.warnes.erp.modules.reporting.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.catalog.dto.response.PriceHistoryResponseDTO;
import com.warnes.erp.modules.catalog.service.ProductService;
import com.warnes.erp.modules.inventory.dto.response.StockMovementResponseDTO;
import com.warnes.erp.modules.inventory.service.StockService;
import com.warnes.erp.modules.reporting.entity.GroupSalesBy;
import com.warnes.erp.modules.reporting.entity.PeriodGranularity;
import com.warnes.erp.modules.reporting.service.PdfService;
import com.warnes.erp.modules.sales.dto.response.*;
import com.warnes.erp.modules.sales.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reportes y Documentos", description = "Generación asíncrona de archivos PDF (Facturas, Tickets y Kardex)")
public class ReportController {

    private final PdfService pdfService;
    private final ProductService productService;
    private final SaleService saleService;
    private final StockService stockService;

    //SALES
    @Operation(summary = "Descargar Factura A4", description = "Retorna el array de bytes de un archivo PDF estructurado para impresión tamaño carta/A4.")
    @GetMapping("/sales/{uuid}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<byte[]> downloadInvoiceA4(@PathVariable UUID uuid) {
        SaleResponseDTO sale = saleService.getSaleByUuid(uuid);

        Map<String, Object> data = new HashMap<>();
        data.put("sale", sale);
        data.put("companyName", "Repuestos Warnes S.A.");
        data.put("printDate", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        byte[] pdfBytes = pdfService.generatePdfFromHtml("invoice-a4", data);
        return buildPdfResponse(pdfBytes, "Factura_" + sale.invoiceNumber() + ".pdf");
    }

    @Operation(summary = "Descargar Ticket Térmico", description = "Retorna el array de bytes de un archivo PDF optimizado para impresora térmica de 80mm.")
    @GetMapping("/sales/{uuid}/ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<byte[]> downloadTicket80mm(@PathVariable UUID uuid) {
        SaleResponseDTO sale = saleService.getSaleByUuid(uuid);

        Map<String, Object> data = new HashMap<>();
        data.put("sale", sale);

        byte[] pdfBytes = pdfService.generatePdfFromHtml("ticket-80mm", data);
        return buildPdfResponse(pdfBytes, "Ticket_" + sale.invoiceNumber() + ".pdf");
    }

    @Operation(summary = "Reporte de las ventas que se completaron por categorias")
    @GetMapping("/sales/by-category")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SaleByCategoryResponseDTO>>> getSaleByCategory(
            @RequestParam(required = false) UUID warehouseUuid,
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to
    ) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleByCategory(warehouseUuid, from, to)));
    }

    @Operation(summary = "Reporte de las ventas que se completaron por periodos (DIAS, SEMANA, MES).")
    @GetMapping("/sales/by-period")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SaleByPeriodResponseDTO>>> getSaleByPeriod(
            @RequestParam(required = false) UUID warehouseUuid,
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to,
            @RequestParam(required = false, defaultValue = "DAILY") PeriodGranularity granularity
            ) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleByPeriod(warehouseUuid, granularity, from, to)));
    }

    @Operation(summary = "Margenes de ganancia segun la agrupacion (CATEGORIAS O PRODUCTO).")
    @GetMapping("/sales/profit-margin")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfitMarginResponseDTO>>> getProfitMargin(
            @RequestParam(required = false) UUID warehouseUuid,
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to,
            @RequestParam(required = false, defaultValue = "CATEGORY")GroupSalesBy groupBy
            ) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getProfitMargin(warehouseUuid, groupBy, from, to)));
    }

    @Operation(summary = "Top de productos mas vendidos.")
    @GetMapping("/sales/by-top-product")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SaleByProductResponseDTO>>> getSaleByProduct(
            @RequestParam(required = false) UUID warehouseUuid,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to
    ) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleByProduct(warehouseUuid, limit, from, to)));
    }

    @Operation(summary = "Historial de precios de un producto por UUID.")
    @GetMapping("/product/{uuid}/price-history")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PriceHistoryResponseDTO>>> getPriceHistory(
            @PathVariable UUID uuid,
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to
    ) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductPriceHistory(uuid, from, to)));
    }

    @Operation(summary = "Descargar Reporte Kardex", description = "Retorna un reporte tabular en PDF con todo el historial de movimientos de un producto.")
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

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}