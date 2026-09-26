package com.warnes.erp.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen del proceso de importación masiva por lote (Bulk/CSV)")
public record BulkImportResponseDTO(
        @Schema(description = "Cantidad total de filas leídas del archivo", example = "1500")
        int totalRowsProcessed,

        @Schema(description = "Cantidad de registros insertados exitosamente", example = "1490")
        int successfulImports,

        @Schema(description = "Cantidad de registros fallidos o ignorados (ej: SKU inexistente)", example = "10")
        int failedOrSkippedRows,

        @Schema(description = "Mensaje general del resultado del proceso", example = "Proceso de importación finalizado.")
        String message
) {}