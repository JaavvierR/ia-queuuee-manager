// File: src/main/java/org/acme/infrastructure/adapters/input/rest/ChatbotController.java
package org.acme.infrastructure.adapters.input.chatbot.rest;

import org.acme.ports.test_cases_ports.ProcessContextPort;
import org.acme.ports.test_cases_ports.ProcessContextApiPort;
import org.acme.ports.test_cases_ports.ProcessContextUiPort;
import org.acme.ports.test_cases_ports.PostmanCollectionPort;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;

@Path("/chatbot")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatbotController {

    private final ProcessContextPort processContextAdapter;
    private final ProcessContextApiPort processContextApiAdapter;
    private final ProcessContextUiPort processContextUiAdapter;
    private final PostmanCollectionPort postmanCollectionAdapter;

    @Inject
    public ChatbotController(
            ProcessContextPort processContextAdapter,
            ProcessContextApiPort processContextApiAdapter,
            ProcessContextUiPort processContextUiAdapter,
            PostmanCollectionPort postmanCollectionAdapter) {
        this.processContextAdapter = processContextAdapter;
        this.processContextApiAdapter = processContextApiAdapter;
        this.processContextUiAdapter = processContextUiAdapter;
        this.postmanCollectionAdapter = postmanCollectionAdapter;
    }

    @POST
    @Path("/process-context")
    public Response processContext(Map<String, Object> inputData) {
        try {
            Object dataObj = inputData.get("data");
            if (!(dataObj instanceof Map)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse("El campo 'data' es requerido y debe ser un objeto.", 400))
                        .build();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;

            String typeContext = (String) data.get("typeContext");
            Object rawResult; // Use Object to capture the raw return type from ports
            Map<String, Object> result; // This will hold the casted/processed result

            // The provided adapter implementations have different processContext signatures
            // compared to their respective port definitions (interface/abstract class).
            // We must call the method signature defined in the injected port type.
            // The adapter implementation is responsible for handling the input object
            // and returning the expected structure.

            switch (Optional.ofNullable(typeContext).orElse("")) {
                case "GenerateTestFunctionalApi":
                    // ProcessContextApiPort defines processContext(Map<String, Object>, String, String, List<Object>) returning Object.
                    // We call it as defined in the interface.
                    String conversationIdApi = (String) data.get("conversation_id");
                    String userMessageApi = null; // Extract if available in inputData structure
                    List<Object> previousMessagesApi = null; // Extract if available
                    rawResult = processContextApiAdapter.processContext(inputData, conversationIdApi, userMessageApi, previousMessagesApi);
                    // Cast the result to Map<String, Object> as expected by the controller logic,
                    // assuming the adapter implementation actually returns this type.
                    if (!(rawResult instanceof Map)) {
                         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(errorResponse("El adaptador ProcessContextApiAdapter no devolvió un Map<String, Object>.", 500))
                            .build();
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> castedResultApi = (Map<String, Object>) rawResult;
                    result = castedResultApi;
                    break;
                case "GenerateTestFunctionalUi":
                    // ProcessContextUiPort defines protected abstract Object processContext(Object inputData).
                    // We call the method signature defined in the abstract class.
                    rawResult = processContextUiAdapter.processContext(inputData);
                     // Cast the result to Map<String, Object> as expected by the controller logic,
                    // assuming the adapter implementation actually returns this type.
                     if (!(rawResult instanceof Map)) {
                         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(errorResponse("El adaptador ProcessContextUiAdapter no devolvió un Map<String, Object>.", 500))
                            .build();
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> castedResultUi = (Map<String, Object>) rawResult;
                    result = castedResultUi;
                    break;
                case "GenerateTestFunctional":
                    // ProcessContextPort defines processContext(Map<String, Object>, String, String, List<String>) returning Map<String, Object>.
                    // This signature matches the expected return type, no cast needed for the result variable.
                    String conversationIdFunc = (String) data.get("conversation_id");
                    String userMessageFunc = null; // Extract if available
                    List<String> previousMessagesFunc = null; // Extract if available
                    result = processContextAdapter.processContext(inputData, conversationIdFunc, userMessageFunc, previousMessagesFunc);
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(errorResponse("El campo 'typeContext' es inválido o no proporcionado.", 400))
                            .build();
            }

            int status = (int) result.getOrDefault("status", 500);
            return Response.status(status).entity(result).build();

        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Error interno del servidor al procesar el contexto: " + e.getMessage(), 500))
                    .build();
        }
    }

    @GET
    @Path("/generate-postman/{conversationId}")
    public Response generatePostmanCollection(@PathParam("conversationId") String conversationId) {
        try {
            Map<String, Object> postmanCollection = postmanCollectionAdapter.generatePostmanCollection(conversationId);
            return Response.ok(postmanCollection).build();
        } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse(e.getMessage(), 404))
                    .build();
        }
        catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse("Error interno del servidor al generar la colección Postman: " + e.getMessage(), 500))
                    .build();
        }
    }

    private Map<String, Object> errorResponse(String message, int code) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", code);
        return error;
    }
}
