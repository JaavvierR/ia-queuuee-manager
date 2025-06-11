package org.acme.infrastructure.adapters.input.textextraction.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

import org.acme.application.ports.input.textextraction.TextExtractionUseCase;
import org.acme.domain.textextraction.model.DocumentProcessingRequest;
import org.acme.domain.textextraction.model.NewDocumentProcessingRequest;
import org.acme.domain.textextraction.model.TextExtractionResult;
import org.jboss.logging.Logger;

@Path("/screening")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TextExtractionController {
    
    private static final Logger logger = Logger.getLogger(TextExtractionController.class);
    
    private final TextExtractionUseCase textExtractionService;
    
    @Inject
    public TextExtractionController(TextExtractionUseCase textExtractionService) {
        this.textExtractionService = textExtractionService;
        logger.info("TextExtractionController inicializado");
    }
    
    @POST
    @Path("/text-extraction")
    public Response processDocument(NewDocumentProcessingRequest request) {
        logger.debug("Recibida solicitud para procesar documento");
        
        if (request == null) {
            logger.warn("Solicitud inválida: documento nulo");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Se requiere un documento para procesar")
                    .build();
        }
        
        // Convertimos el único objeto a una lista para mantener compatibilidad interna
        List<NewDocumentProcessingRequest> requests = new ArrayList<>();
        requests.add(request);
        
        List<TextExtractionResult> results = new ArrayList<>();
        
        // Validaciones básicas para el único documento
        if (request.getSource() == null) {
            logger.warn("Solicitud inválida: fuente nula");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La fuente del documento es obligatoria")
                    .build();
        }
        
        if (request.getSource().getData() == null || request.getSource().getData().isEmpty()) {
            logger.warn("Solicitud inválida: datos base64 faltantes o vacíos");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El contenido en base64 es obligatorio")
                    .build();
        }
        
        if (request.getSource().getFile_name() == null || request.getSource().getFile_name().isEmpty()) {
            logger.warn("Solicitud inválida: nombre de archivo faltante o vacío");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nombre del archivo es obligatorio")
                    .build();
        }
        
        if (request.getFile_type() == null || request.getFile_type().isEmpty()) {
            logger.warn("Solicitud inválida: tipo de archivo faltante o vacío");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El tipo de archivo es obligatorio")
                    .build();
        }
        
        try {
            // Convertir al formato anterior
            DocumentProcessingRequest oldRequest = request.toDocumentProcessingRequest();
            
            // Calcular el tamaño del archivo (aproximado)
            byte[] decodedData = Base64.getDecoder().decode(request.getSource().getData());
            long fileSize = decodedData.length;
            
            // Procesar el documento - ahora devuelve directamente la estructura deseada
            TextExtractionResult result = textExtractionService.processDocument(oldRequest);
            
            // Ya no necesitamos convertir a un nuevo formato, porque TextExtractionResult ya tiene la estructura deseada
            results.add(result);
            logger.info("Documento procesado exitosamente: " + request.getSource().getFile_name());
            
        } catch (Exception e) {
            logger.error("Error al procesar el documento", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al procesar el documento: " + e.getMessage())
                    .build();
        }
        
        // Si solo hay un resultado, devolver el objeto directamente en lugar de una lista
        if (results.size() == 1) {
            return Response.ok(results.get(0)).build();
        } else {
            return Response.ok(results).build();
        }
    }
}