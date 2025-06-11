package org.acme.infrastructure.adapters.input.minio.rest;

import org.acme.application.ports.input.minio.MinioUseCase;
import org.acme.domain.minio.exception.MinioProcessingException;
import org.acme.domain.minio.model.FileUploadResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/screening")
@Produces(MediaType.APPLICATION_JSON)
public class MinioController {
    
    private static final Logger logger = Logger.getLogger(MinioController.class);
    private static final List<String> ALLOWED_TYPES = Arrays.asList("docx", "pdf", "jpg", "png", "jpeg", "yaml", "json");
    
    private final MinioUseCase minioService;
    
    @Inject
    public MinioController(MinioUseCase minioService) {
        this.minioService = minioService;
    }
    
    @GET
    public Response index() {
        return Response.ok()
                .entity(Map.of(
                        "message", "Welcome to the API of storage",
                        "Supported format", ALLOWED_TYPES
                ))
                .build();
    }
    
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @RestForm("file") FileUpload file,
            @RestForm("file_name") String fileName,
            @RestForm("file_type") String fileType,
            @RestForm("bucket_name") String bucketName) {
        
        try {
            logger.info("Uploaded file name: " + file.fileName());
            logger.info("Uploaded file path: " + file.uploadedFile());
            
            // Use provided file name or fall back to original name
            String finalFileName = (fileName != null && !fileName.isEmpty()) ? fileName : file.fileName();
            
            // Validate file type
            String fileExtension = getFileExtension(finalFileName);
            if (!ALLOWED_TYPES.contains(fileExtension.toLowerCase())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Tipo de archivo no permitido. Formatos aceptados: " + ALLOWED_TYPES))
                        .build();
            }
            
            // Upload file to MinIO
            String fileUrl = minioService.uploadFile(file.uploadedFile().toString(), finalFileName, bucketName);
            
            // Create response
            FileUploadResponse response = new FileUploadResponse(
                    fileUrl, 
                    finalFileName, 
                    (fileType != null && !fileType.isEmpty()) ? fileType : "unknown", 
                    bucketName
            );
            
            return Response.ok(response).build();
            
        } catch (MinioProcessingException e) {
            logger.error("Error al procesar MinIO", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al subir archivo: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.error("Error inesperado al manejar la carga del archivo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al subir archivo: " + e.getMessage()))
                    .build();
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
}