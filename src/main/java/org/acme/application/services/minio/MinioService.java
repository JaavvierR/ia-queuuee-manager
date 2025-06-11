package org.acme.application.services.minio;

import org.acme.application.ports.input.minio.MinioUseCase;
import org.acme.application.ports.output.minio.MinioPort;
import org.acme.domain.minio.exception.MinioProcessingException;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MinioService implements MinioUseCase {
    
    private static final Logger logger = Logger.getLogger(MinioService.class);
    
    private final MinioPort minioRepository;
    
    @Inject
    public MinioService(MinioPort minioRepository) {
        logger.info("Inicializando MinioService");
        this.minioRepository = minioRepository;
    }
    
    @Override
    public String uploadFile(String filePath, String fileName, String bucketName) {
        logger.debug("Iniciando carga de archivo: " + filePath + " en bucket: " + bucketName);
        try {
            String result = minioRepository.uploadFile(filePath, fileName, bucketName);
            if (result != null) {
                logger.info("Archivo subido exitosamente. URL: " + result);
                return result;
            } else {
                logger.error("Error al subir el archivo");
                throw new MinioProcessingException("Error al subir el archivo");
            }
        } catch (Exception e) {
            logger.error("Error al subir archivo", e);
            throw new MinioProcessingException("Error al subir archivo: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean checkFileExists(String fileUuid) {
        logger.debug("Verificando si el archivo con UUID " + fileUuid + " existe en MinIO");
        try {
            return minioRepository.checkFileExists(fileUuid);
        } catch (Exception e) {
            logger.error("Error al verificar archivo", e);
            throw new MinioProcessingException("Error al verificar archivo: " + e.getMessage(), e);
        }
    }
}