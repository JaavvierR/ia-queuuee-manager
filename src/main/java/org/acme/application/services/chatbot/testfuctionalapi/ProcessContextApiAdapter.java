package org.acme.application.services.chatbot.testfuctionalapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.ports.test_cases_ports.ProcessContextApiPort;
import org.acme.ports.output_ports.MongoDBPort;
import org.acme.ports.conversation_ports.NameHistoryGeneratorPort;
import org.acme.ports.conversation_ports.ConversationManagementPort;
import org.acme.ports.output_ports.LLMPort;
import org.acme.ports.test_cases_ports.CountTokensPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

public class ProcessContextApiAdapter implements ProcessContextApiPort {

    private final LLMPort llm;
    private final MongoDBPort mongoRepo;
    private final NameHistoryGeneratorPort nameHistoryGenerator;
    private final ConversationManagementPort conversationManager;
    private final CountTokensPort countTokens;
    private final Map<String, List<Map<String, Object>>> conversationHistory = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ProcessContextApiAdapter.class);
    private final String apiTextStorageUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProcessContextApiAdapter(
            LLMPort llm,
            MongoDBPort mongoRepo,
            NameHistoryGeneratorPort nameHistoryGenerator,
            ConversationManagementPort conversationManager,
            CountTokensPort countTokens
    ) {
        this.llm = llm;
        this.mongoRepo = mongoRepo;
        this.nameHistoryGenerator = nameHistoryGenerator;
        this.conversationManager = conversationManager;
        this.countTokens = countTokens;
        this.apiTextStorageUrl = System.getenv().getOrDefault("API_TEXT_STORAGE_URL", "http://qava-cn-ai-text-extraction:7272/process-file");
        logger.info("Conexion establecida a: {}", this.apiTextStorageUrl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> processContext(Map<String, Object> inputData, String conversationId, String userMessage, List<Object> previousMessages) {
        logger.debug("Procesando test_type 'context'...");

        Object dataObj = inputData.get("data");
        if (!(dataObj instanceof Map)) {
            logger.error("El campo 'data' no es un Map.");
            return errorResponse("El campo 'data' debe ser un objeto Map<String, Object>.", 400);
        }
        Map<String, Object> data;
        try {
            data = (Map<String, Object>) dataObj;
        } catch (ClassCastException e) {
            logger.error("No se pudo convertir 'data' a Map<String, Object>: {}", e.getMessage());
            return errorResponse("El campo 'data' debe ser un objeto Map<String, Object>.", 400);
        }

        String convId = (String) Optional.ofNullable(data.get("conversation_id")).orElse(UUID.randomUUID().toString());
        String dateCreated = (String) Optional.ofNullable(data.get("created")).orElse(Instant.now().toString());
        Object messagesObj = data.getOrDefault("message", new ArrayList<>());
        List<Map<String, Object>> messages = new ArrayList<>();
        if (messagesObj instanceof List) {
            for (Object item : (List<?>) messagesObj) {
                if (item instanceof Map) {
                    try {
                        messages.add((Map<String, Object>) item);
                    } catch (ClassCastException e) {
                        logger.warn("Un elemento de 'message' no es Map<String, Object>: {}", e.getMessage());
                    }
                }
            }
        }

        String nameHistory = (String) data.get("nameHistory");
        Object step = data.get("step");
        Object selectValue = data.get("selectValue");
        Object testType = data.get("testType");
        Object typeContext = data.get("typeContext");
        String userId = (String) data.get("userID");
        Object isSelectBlockObj = data.getOrDefault("isSelectBlock", false);
        Object contextLevel = data.get("contextLevel");

        boolean isSelectBlock;
        if (isSelectBlockObj instanceof Boolean) {
            isSelectBlock = (Boolean) isSelectBlockObj;
        } else {
            isSelectBlock = Boolean.parseBoolean(isSelectBlockObj.toString());
        }

        if (!(isSelectBlockObj instanceof Boolean)) {
            logger.error("El valor de 'isSelectBlock' no es booleano.");
            return errorResponse("El valor de 'isSelectBlock' debe ser booleano.", 400);
        }

        int maxTokens = 3500;

        logger.debug("conversation_id: {}, date_created: {}, name_history: {}, user_id: {}", convId, dateCreated, nameHistory, userId);

        if (userId == null || userId.isEmpty()) {
            logger.error("No se proporcionó un userID válido.");
            return errorResponse("Se requiere un userID válido para crear la conversación.", 400);
        }

        // Verificar si el userID existe en la base de datos
        try {
            logger.debug("Verificando existencia del userID en la base de datos...");
            List<Map<String, Object>> allUsers = mongoRepo.getAllUsers();
            boolean userExists = allUsers.stream().anyMatch(user -> userId.equals(user.get("_id")));
            if (!userExists) {
                logger.error("El userID {} no existe en la base de datos.", userId);
                return errorResponse("El userID proporcionado no existe en la base de datos.", 404);
            }
            logger.debug("El userID {} existe en la base de datos.", userId);
        } catch (Exception e) {
            logger.error("Error al verificar el userID en la base de datos: {}", e.getMessage());
            return errorResponse("Error al verificar el userID en la base de datos.", 500);
        }

        String processedContent = null;
        Map<String, Object> message = messages.isEmpty() ? null : messages.get(messages.size() - 1);

        if (message != null && "user".equals(message.get("role"))) {
            Object content = message.get("content");
            if (content instanceof Map && ((Map<?, ?>) content).containsKey("file_type")) {
                try {
                    logger.debug("Procesando archivo con el microservicio: {}", content);
                    Map<String, Object> processedData = callTextExtractionService((Map<String, Object>) content);
                    if (processedData != null && processedData.containsKey("source")) {
                        message.put("content", processedData);
                        Map<String, Object> source = (Map<String, Object>) processedData.get("source");
                        processedContent = (String) source.getOrDefault("data", "");
                        logger.debug("Archivo procesado y actualizado: {}", message.get("content"));
                    } else {
                        logger.error("La respuesta del microservicio no tiene el formato esperado");
                        return errorResponse("La respuesta del microservicio no tiene el formato esperado", 500);
                    }
                } catch (Exception e) {
                    logger.error("Error al llamar al microservicio: {}", e.getMessage());
                    return errorResponse("Error al llamar al microservicio: " + e.getMessage(), 500);
                }
            } else {
                processedContent = content != null ? content.toString() : "";
            }
        }

        // Validar el contexto del usuario con el contenido procesado
        boolean isValidContext;
        try {
            isValidContext = validateContext(processedContent);
            logger.debug("Validez del contexto: {}", isValidContext);
        } catch (Exception e) {
            logger.error("Error al validar el contexto: {}", e.getMessage());
            return errorResponse("Error al validar el contexto: " + e.getMessage(), 500);
        }

        // Si el contexto es válido y no hay un nombre de historia, generarlo
        if (isValidContext && (nameHistory == null || nameHistory.isEmpty())) {
            try {
                nameHistory = nameHistoryGenerator.generateNameHistory(processedContent);
                logger.debug("Nombre de historia generado: {}", nameHistory);
            } catch (Exception e) {
                logger.error("Error al generar el nombre de historia: {}", e.getMessage());
                return errorResponse("Error al generar el nombre de historia: " + e.getMessage(), 500);
            }
        } else if (nameHistory == null || nameHistory.isEmpty()) {
            nameHistory = Arrays.stream(processedContent != null ? processedContent.trim().split("\\s+") : new String[0])
                    .limit(5)
                    .collect(Collectors.joining(" "));
            logger.debug("Nombre de historia generado sin IA: {}", nameHistory);
        }

        // Crear nuevos mensajes de conversación
        List<Map<String, Object>> newMessages = new ArrayList<>();
        try {
            for (Map<String, Object> msg : messages) {
                if (msg.containsKey("role") && msg.containsKey("content")) {
                    Map<String, Object> newMsg = new HashMap<>();
                    newMsg.put("id", UUID.randomUUID().toString());
                    newMsg.put("role", msg.get("role"));
                    newMsg.put("content", msg.get("content"));
                    if (msg.containsKey("validate")) {
                        newMsg.put("validate", msg.get("validate"));
                    }
                    newMessages.add(newMsg);
                }
            }
            logger.debug("Nuevos mensajes creados: {}", newMessages);
        } catch (Exception e) {
            logger.error("Error al crear nuevos mensajes: {}", e.getMessage());
            return errorResponse("Error al crear nuevos mensajes: " + e.getMessage(), 500);
        }

        // Obtener solo los mensajes validados previos
        List<Map<String, Object>> previousValidatedMessages = newMessages.stream()
                .limit(Math.max(0, newMessages.size() - 1))
                .filter(msg -> msg.containsKey("validate"))
                .collect(Collectors.toList());

        List<String> previousValidatedIds = previousValidatedMessages.stream()
                .map(msg -> (String) msg.get("id"))
                .collect(Collectors.toList());

        if (!newMessages.isEmpty() && newMessages.get(newMessages.size() - 1).containsKey("validate")) {
            newMessages.get(newMessages.size() - 1).put("validated_message_ids", previousValidatedIds);
        }

        // Crear el mensaje de validación
        Map<String, Object> validationMessage = new HashMap<>();
        validationMessage.put("id", UUID.randomUUID().toString());
        validationMessage.put("role", "assistant");
        validationMessage.put("content", isValidContext
                ? "¡Gracias por el contexto! Ahora necesito que me proporciones la Historia de Usuario (HU) para poder generar los casos de prueba."
                : "Parece que el contexto proporcionado no está suficientemente claro para la generación de casos de prueba para APIs. Para ayudarte mejor, el contexto debe incluir detalles técnicos específicos sobre la API, como su funcionalidad, los endpoints disponibles, los parámetros requeridos y las respuestas esperadas. Asegúrate también de que el contexto describa las interacciones esperadas y cualquier restricción técnica relevante. Esto facilitará la creación de casos de prueba precisos."
        );
        validationMessage.put("validate", isValidContext);
        validationMessage.put("isHu", true);

        List<Map<String, Object>> updatedMessages = new ArrayList<>(newMessages);
        updatedMessages.add(validationMessage);
        logger.debug("Conversaciones actualizadas: {}", updatedMessages);

        // Contar los tokens de entrada
        int tokensInput = 0;
        try {
            for (Map<String, Object> msg : messages) {
                if ("user".equals(msg.get("role"))) {
                    Object content = msg.get("content");
                    if (content instanceof Map) {
                        Map<?, ?> contentMap = (Map<?, ?>) content;
                        if (contentMap.containsKey("source") && ((Map<?, ?>) contentMap.get("source")).containsKey("data")) {
                            tokensInput += countTokens.countTokens(((Map<?, ?>) contentMap.get("source")).get("data").toString());
                        } else if (contentMap.containsKey("data")) {
                            tokensInput += countTokens.countTokens(contentMap.get("data").toString());
                        }
                    } else if (content instanceof List) {
                        for (Object item : (List<?>) content) {
                            if (item instanceof Map) {
                                Map<?, ?> itemMap = (Map<?, ?>) item;
                                if (itemMap.containsKey("source") && ((Map<?, ?>) itemMap.get("source")).containsKey("data")) {
                                    tokensInput += countTokens.countTokens(((Map<?, ?>) itemMap.get("source")).get("data").toString());
                                }
                            }
                        }
                    } else if (content != null) {
                        tokensInput += countTokens.countTokens(content.toString());
                    }
                }
            }
            logger.debug("Tokens de entrada: {}", tokensInput);
        } catch (Exception e) {
            logger.error("Error al contar los tokens de entrada: {}", e.getMessage());
            return errorResponse("Error al contar los tokens de entrada: " + e.getMessage(), 500);
        }

        // Contar los tokens de salida (mensaje de validación)
        int tokensOutput;
        try {
            tokensOutput = countTokens.countTokens(validationMessage.get("content").toString());
            logger.debug("Tokens de salida (validación): {}", tokensOutput);
        } catch (Exception e) {
            logger.error("Error al contar los tokens de salida: {}", e.getMessage());
            return errorResponse("Error al contar los tokens de salida: " + e.getMessage(), 500);
        }

        int totalTokens = tokensInput + tokensOutput;
        logger.debug("Total de tokens: {}", totalTokens);

        // Crear la respuesta con los nuevos campos
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("_id", convId);
        responseData.put("created", dateCreated);
        responseData.put("conversation_id", convId);
        responseData.put("nameHistory", nameHistory);
        responseData.put("userID", userId);
        responseData.put("step", step);
        responseData.put("selectValue", selectValue);
        responseData.put("contextLevel", contextLevel);
        responseData.put("isSelectBlock", isSelectBlock);
        responseData.put("typeContext", typeContext);
        responseData.put("testType", testType);
        responseData.put("tokens_input", tokensInput);
        responseData.put("tokens_output", tokensOutput);
        responseData.put("tokens", totalTokens);
        responseData.put("max_tokens", maxTokens);
        responseData.put("message", updatedMessages);

        // Guardar en MongoDB
        Map<String, Object> messageToSave = new HashMap<>();
        messageToSave.put("_id", convId);
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put("conversationId", convId);
        dataToSave.put("nameHistory", nameHistory);
        dataToSave.put("userID", userId);
        dataToSave.put("step", step);
        dataToSave.put("selectValue", selectValue);
        dataToSave.put("typeContext", typeContext);
        dataToSave.put("testType", testType);
        dataToSave.put("tokens_input", tokensInput);
        dataToSave.put("tokens_output", tokensOutput);
        dataToSave.put("tokens", totalTokens);
        dataToSave.put("max_tokens", maxTokens);
        dataToSave.put("message", updatedMessages);
        messageToSave.put("data", dataToSave);

        try {
            mongoRepo.saveConversation(messageToSave);
            logger.info("Conversaciones guardadas en MongoDB.");
        } catch (Exception e) {
            logger.error("Error al guardar la conversación en MongoDB: {}", e.getMessage());
            return errorResponse("Error al guardar la conversación en MongoDB: " + e.getMessage(), 500);
        }

        conversationHistory.put(convId, updatedMessages);
        conversationManager.saveConversation(convId, processedContent, nameHistory, isValidContext);

        logger.info("Conversación guardada exitosamente.");

        Map<String, Object> result = new HashMap<>();
        result.put("data", responseData);
        result.put("status", 200);
        return result;
    }

    @Override
    public boolean validateContext(String context) {
        logger.debug("Validando contexto: {}", context);
        String validationPrompt = promptValidationContext(context);
        logger.debug("Prompt de validación generado: {}", validationPrompt);

        Object responseTuple = llm.generateFunctionalApiTestResponse(validationPrompt, null);

        String response;
        if (responseTuple instanceof Object[] && ((Object[]) responseTuple).length > 0) {
            response = ((Object[]) responseTuple)[0].toString();
        } else {
            response = responseTuple.toString();
        }

        logger.debug("Respuesta del LLM: {}", response);

        String responseNormalized = response.trim().toLowerCase();

        if ("true".equals(responseNormalized)) {
            return true;
        } else if ("false".equals(responseNormalized)) {
            return false;
        } else {
            logger.warn("Respuesta inesperada del LLM: {}", responseNormalized);
            return false;
        }
    }

    @Override
    public String promptValidationContext(String context) {
        return "Acepta como contexto técnico un documento en formato Swagger, GraphQL (en estructura JSON) u OpenAPI, y también texto que describa una API. Responde únicamente con 'true' o 'false':\n\n¿El siguiente texto representa código de un Swagger, GraphQL en JSON, OpenAPI, o al menos una descripción textual de una API?\n\n" + context;
    }

    private Map<String, Object> errorResponse(String message, int status) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", status);
        return error;
    }

    // Simulación de llamada HTTP al microservicio de extracción de texto
    private Map<String, Object> callTextExtractionService(Map<String, Object> content) {
        // Aquí deberías implementar la llamada HTTP real usando HttpClient, OkHttp, etc.
        // Por ahora, retorna null o un mock.
        return null;
    }
}