package com.warnes.erp.modules.reporting.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.modules.reporting.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final SpringTemplateEngine templateEngine;

    @Override
    public byte[] generatePdfFromHtml(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 1. Thymeleaf procesa el HTML inyectando los datos del DTO
            String processedHtml = templateEngine.process(templateName, context);

            // 2. Flying Saucer convierte el String XHTML a PDF binario
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(processedHtml);
            renderer.layout();
            renderer.createPDF(outputStream, false);
            renderer.finishPDF();

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error crítico al generar el PDF para la plantilla: {}", templateName, e);
            throw new BusinessRuleException("No se pudo generar el documento PDF. Revise los logs del sistema.");
        }
    }
}