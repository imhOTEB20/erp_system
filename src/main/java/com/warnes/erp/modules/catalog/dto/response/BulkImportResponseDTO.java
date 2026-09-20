package com.warnes.erp.modules.catalog.dto.response;

public record BulkImportResponseDTO(
        int totalRowsProcessed,
        int successfulImports,
        int failedOrSkippedRows,
        String message
) {}