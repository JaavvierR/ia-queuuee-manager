package org.acme.application.ports.output.textextraction;

import java.nio.file.Path;

public interface DocumentProcessorPort {
    /**
     * Extrae texto de un documento según su tipo.
     * 
     * @param filePath ruta al archivo temporal
     * @param fileType tipo de archivo (pdf, docx, etc.)
     * @param language idioma para OCR (opcional, puede ser nulo)
     * @return el texto extraído del documento
     */
    String extractText(Path filePath, String fileType, String language);
}