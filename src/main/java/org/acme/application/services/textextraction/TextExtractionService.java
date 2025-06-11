package org.acme.application.services.textextraction;

import org.acme.application.ports.input.textextraction.TextExtractionUseCase;
import org.acme.application.ports.input.minio.MinioUseCase;
import org.acme.application.ports.output.textextraction.DocumentProcessorPort;
import org.acme.domain.textextraction.exception.TextExtractionException;
import org.acme.domain.textextraction.model.DocumentProcessingRequest;
import org.acme.domain.textextraction.model.TextExtractionResult;
import org.acme.domain.textextraction.model.SourceResponse;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Singleton
public class TextExtractionService implements TextExtractionUseCase {
    
    private static final Logger logger = Logger.getLogger(TextExtractionService.class);
    
    private final DocumentProcessorPort documentProcessor;
    private final MinioUseCase minioService;
    
    @Inject
    public TextExtractionService(DocumentProcessorPort documentProcessor, MinioUseCase minioService) {
        logger.info("Inicializando TextExtractionService con implementación predeterminada");
        this.documentProcessor = documentProcessor;
        this.minioService = minioService;
    }
    
    @Override
    public TextExtractionResult processDocument(DocumentProcessingRequest request) {
        logger.debug("Iniciando procesamiento de documento: " + request.getFileName());
        
        Path tempFile = null;
        try {
            // Decodificar contenido base64
            byte[] fileContent = Base64.getDecoder().decode(request.getBase64Content());
            
            // Calcular tamaño del archivo
            long fileSize = fileContent.length;
            
            // Crear archivo temporal
            tempFile = Files.createTempFile("document-", "." + request.getFileType());
            Files.write(tempFile, fileContent);
            logger.debug("Archivo temporal creado en: " + tempFile);
            
            // Extraer texto mediante el DocumentProcessorPort (que ahora usará la implementación Python)
            String extractedText = documentProcessor.extractText(
                    tempFile, 
                    request.getFileType(),
                    request.getLanguage());
            
            logger.debug("Texto extraído mediante servicio (longitud): " + (extractedText != null ? extractedText.length() : 0));
            
            // Subir archivo a MinIO
            String fileUrl = minioService.uploadFile(
                    tempFile.toString(),
                    request.getFileName(),
                    null);
            
            // Construir respuesta
            SourceResponse sourceResponse = new SourceResponse(
                extractedText,
                "application/" + request.getFileType(), 
                "success",
                "base64"
            );
            
            TextExtractionResult result = new TextExtractionResult(
                request.getFileName(),
                fileSize,
                request.getFileType(),
                fileUrl,
                sourceResponse
            );
            
            logger.info("Documento procesado exitosamente: " + request.getFileName());
            
            return result;
            
        } catch (IOException e) {
            logger.error("Error al procesar archivo", e);
            throw new TextExtractionException("Error al procesar el archivo: " + e.getMessage(), e);
        } finally {
            // Limpiar archivo temporal
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    logger.debug("Archivo temporal eliminado");
                } catch (IOException e) {
                    logger.warn("No se pudo eliminar el archivo temporal", e);
                }
            }
        }
    }
}