package com.warnes.erp.modules.reporting.service;

import java.util.Map;

public interface PdfService {
    /**
     * Genera un array de bytes (PDF) a partir de una plantilla Thymeleaf.
     * @param templateName Nombre del archivo HTML (sin la extensión .html) ubicado en resources/templates/
     * @param data Variables dinámicas que se inyectarán en la plantilla
     * @return PDF compilado en memoria
     */
    byte[] generatePdfFromHtml(String templateName, Map<String, Object> data);
}