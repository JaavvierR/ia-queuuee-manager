package org.acme.application.ports.output.minio;

public interface MinioPort {
    /**
     * Sube un archivo a MinIO
     * 
     * @param filePath Ruta del archivo a subir
     * @param fileName Nombre del archivo
     * @param bucketName Nombre del bucket
     * @return URL del archivo subido
     */
    String uploadFile(String filePath, String fileName, String bucketName);
    
    /**
     * Verifica si un archivo existe en MinIO
     * 
     * @param fileUuid UUID del archivo a verificar
     * @return true si el archivo existe, false en caso contrario
     */
    boolean checkFileExists(String fileUuid);
}