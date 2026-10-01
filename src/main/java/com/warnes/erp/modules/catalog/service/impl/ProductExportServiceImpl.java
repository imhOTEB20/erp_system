package com.warnes.erp.modules.catalog.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.modules.catalog.dto.response.ProductResponseDTO;
import com.warnes.erp.modules.catalog.service.ProductExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ProductExportServiceImpl implements ProductExportService {

    private static final String[] HEADERS = {
            "UUID", "SKU", "Código de Barras", "Nombre", "Categoría",
            "Marca", "Impuesto", "Servicio", "Kit", "Costo",
            "Margen (%)", "Precio de Venta"
    };

    @Override
    public byte[] exportProductsToExcel(List<ProductResponseDTO> products) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Catálogo de Productos");
            createHeaderRow(sheet, workbook);

            int rowIdx = 1;
            for (ProductResponseDTO product : products) {
                Row row = sheet.createRow(rowIdx++);
                populateRow(row, product);
            }

            // Auto-ajustar el ancho de las columnas (opcional, pero da un acabado profesional)
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new BusinessRuleException("Error interno al generar el archivo Excel de productos.");
        }
    }

    private void createHeaderRow(Sheet sheet, Workbook workbook) {
        Row headerRow = sheet.createRow(0);

        // Estilo negrita para la cabecera
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int col = 0; col < HEADERS.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(HEADERS[col]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void populateRow(Row row, ProductResponseDTO product) {
        row.createCell(0).setCellValue(product.uuid() != null ? product.uuid().toString() : "");
        row.createCell(1).setCellValue(product.sku() != null ? product.sku() : "");
        row.createCell(2).setCellValue(product.barcode() != null ? product.barcode() : "");
        row.createCell(3).setCellValue(product.name() != null ? product.name() : "");

        // Extracción segura del nombre de las entidades anidadas
        row.createCell(4).setCellValue(product.category() != null ? product.category().name() : "Sin Categoría");
        row.createCell(5).setCellValue(product.brand() != null ? product.brand().name() : "Sin Marca");
        row.createCell(6).setCellValue(product.taxType() != null ? product.taxType().name() : "Sin Impuesto");

        row.createCell(7).setCellValue(product.service() ? "SÍ" : "NO");
        row.createCell(8).setCellValue(product.kit() ? "SÍ" : "NO");

        // Datos numéricos (Costo, Margen, Precio)
        if (product.costPrice() != null) row.createCell(9).setCellValue(product.costPrice().doubleValue());
        if (product.profitMargin() != null) row.createCell(10).setCellValue(product.profitMargin().doubleValue());
        if (product.salePrice() != null) row.createCell(11).setCellValue(product.salePrice().doubleValue());
    }
}