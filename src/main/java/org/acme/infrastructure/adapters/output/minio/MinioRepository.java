package org.acme.infrastructure.adapters.output.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import org.acme.application.ports.output.minio.MinioPort;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class MinioRepository implements MinioPort {
    
    private static final Logger logger = Logger.getLogger(MinioRepository.class);
    
    @ConfigProperty(name = "minio.host", defaultValue = "minio")
    String minioHost;
    
    @ConfigProperty(name = "minio.port", defaultValue = "6020")
    String minioPort;
    
    @ConfigProperty(name = "minio.access.key", defaultValue = "minioadmin")
    String accessKey;
    
    @ConfigProperty(name = "minio.secret.key", defaultValue = "minioadmin")
    String secretKey;
    
    @ConfigProperty(name = "minio.bucket.name", defaultValue = "document-bucket")
    String defaultBucketName;
    
    private MinioClient minioClient;
    
    @PostConstruct
    public void initialize() {
        logger.info("Inicializando MinioRepository");
        
        try {
            // Inicializar cliente MinIO
            minioClient = MinioClient.builder()
                    .endpoint("http://" + minioHost + ":" + minioPort)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // Verificar si el bucket existe y crearlo si es necesario
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(defaultBucketName).build())) {
                logger.debug("Bucket " + defaultBucketName + " no existe, creándolo");
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(defaultBucketName).build());
                logger.info("Bucket " + defaultBucketName + " creado exitosamente");
            } else {
                logger.debug("Bucket " + defaultBucketName + " ya existe");
            }
        } catch (MinioException e) {
            logger.error("Error al inicializar MinioClient", e);
            throw new RuntimeException("Error al inicializar cliente MinIO", e);
        } catch (Exception e) {
            logger.error("Error inesperado al inicializar MinioClient", e);
            throw new RuntimeException("Error inesperado al inicializar cliente MinIO", e);
        }
    }
    
    /**
     * Genera un nombre de archivo único usando UUID manteniendo la extensión original.
     * 
     * @param originalFilename Nombre original del archivo
     * @return Tupla con el UUID y el nombre completo del archivo
     */
    private Pair<String, String> generateUniqueFilename(String originalFilename) {
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }
        
        String fileUuid = UUID.randomUUID().toString() + fileExtension;
        return new Pair<>(fileUuid, fileUuid);
    }
    
    @Override
    public String uploadFile(String filePath, String fileName, String bucketName) {
        if (bucketName == null) {
            bucketName = defaultBucketName;
        }
        
        try {
            logger.debug("Iniciando carga de archivo: " + filePath);
            
            // Generar nuevo nombre con UUID
            Pair<String, String> fileInfo = generateUniqueFilename(fileName);
            String fileUuid = fileInfo.getFirst();
            String newFilename = fileInfo.getSecond();
            
            logger.debug("Nombre generado con UUID: " + newFilename);
            
            // Subir archivo con el nuevo nombre
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newFilename)
                            .filename(filePath)
                            .build()
            );
            
            // Generar la URL del archivo en MinIO
            String fileUrl = "http://" + minioHost + ":" + minioPort + "/" + bucketName + "/" + newFilename;
            
            return fileUrl;
        } catch (MinioException e) {
            logger.error("Error de MinIO al subir archivo", e);
            throw new RuntimeException("Error al subir archivo a MinIO", e);
        } catch (Exception e) {
            logger.error("Error inesperado al subir archivo", e);
            throw new RuntimeException("Error inesperado al subir archivo", e);
        }
    }
    
    @Override
    public boolean checkFileExists(String fileUuid) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(defaultBucketName)
                            .object(fileUuid)
                            .build()
            );
            
            logger.info("El archivo con UUID " + fileUuid + " existe en el bucket " + defaultBucketName);
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                logger.warn("El archivo con UUID " + fileUuid + " no existe en el bucket " + defaultBucketName);
                return false;
            } else {
                logger.error("Error al acceder al archivo con UUID " + fileUuid, e);
                throw new RuntimeException("Error al verificar archivo en MinIO", e);
            }
        } catch (Exception e) {
            logger.error("Error inesperado al verificar archivo", e);
            throw new RuntimeException("Error inesperado al verificar archivo en MinIO", e);
        }
    }
    
    /**
     * Clase interna simple para manejar un par de valores
     */
    private static class Pair<A, B> {
        private final A first;
        private final B second;
        
        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
        
        public A getFirst() {
            return first;
        }
        
        public B getSecond() {
            return second;
        }
    }
}