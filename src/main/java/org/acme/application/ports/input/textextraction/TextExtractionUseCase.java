package org.acme.application.ports.input.textextraction;

import org.acme.domain.textextraction.model.DocumentProcessingRequest;
import org.acme.domain.textextraction.model.TextExtractionResult;

public interface TextExtractionUseCase {
    /**
     * Procesa un documento en base64, extrae su texto y lo almacena en Minio.
     * 
     * @param request objeto con la información del documento a procesar
     * @return resultado del procesamiento con el texto extraído y la información del archivo subido
     */
    TextExtractionResult processDocument(DocumentProcessingRequest request);
}