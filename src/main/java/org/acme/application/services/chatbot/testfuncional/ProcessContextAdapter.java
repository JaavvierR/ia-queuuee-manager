package org.acme.application.services.chatbot.testfuncional;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import org.acme.ports.test_cases_ports.ProcessContextPort;
import org.acme.ports.output_ports.MongoDBPort;
import org.acme.ports.conversation_ports.NameHistoryGeneratorPort;
import org.acme.ports.conversation_ports.ConversationManagementPort;
import org.acme.ports.output_ports.LLMPort;
import org.acme.ports.test_cases_ports.CountTokensPort;

public class ProcessContextAdapter implements ProcessContextPort {

    private final LLMPort llm;
    private final NameHistoryGeneratorPort nameHistoryGenerator;
    private final CountTokensPort countTokens;
    private final MongoDBPort mongoRepo;
    private final ConversationManagementPort conversationManager;
    private final Map<String, List<Map<String, Object>>> conversationHistory = new HashMap<>();
    private final Logger logger = Logger.getLogger(ProcessContextAdapter.class.getName());

    public ProcessContextAdapter(
            LLMPort llm,
            MongoDBPort mongoRepo,
            NameHistoryGeneratorPort nameHistoryGenerator,
            ConversationManagementPort conversationManager,
            CountTokensPort countTokens
    ) {
        this.llm = llm;
        this.nameHistoryGenerator = nameHistoryGenerator;
        this.countTokens = countTokens;
        this.mongoRepo = mongoRepo;
        this.conversationManager = conversationManager;
        logger.info("ProcessContextAdapter initialized");
    }

    @Override
    public Map<String, Object> processContext(
            Map<String, Object> inputData,
            String conversationId,
            String userMessage,
            List<String> previousMessages
    ) {
        logger.fine("Procesando test_type 'context'...");

        Object dataObj = inputData.get("data");
        Map<String, Object> data;
        if (dataObj instanceof Map) {
            // Suppress unchecked warning for this cast
            @SuppressWarnings("unchecked")
            Map<String, Object> safeData = (Map<String, Object>) dataObj;
            data = safeData;
        } else {
            logger.severe("'data' field is missing or not a Map<String, Object>.");
            return errorResponse("'data' field is missing or invalid.", 400);
        }
        String convId = data.getOrDefault("conversation_id", UUID.randomUUID().toString()).toString();
        String dateCreated = data.getOrDefault("created", Instant.now().toString()).toString();
        Object messagesObj = data.getOrDefault("message", new ArrayList<>());
        List<Map<String, Object>> messages;
        if (messagesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> safeMessages = (List<Map<String, Object>>) messagesObj;
            messages = safeMessages;
        } else {
            messages = new ArrayList<>();
        }
        String nameHistory = (String) data.get("nameHistory");
        Object step = data.get("step");
        Object selectValue = data.get("selectValue");
        String testType = (String) data.get("testType");
        String typeContext = (String) data.get("typeContext");
        String userId = (String) data.get("userID");
        Object contextLevel = data.get("contextLevel");
        int maxTokens = 3500;
        Object isSelectBlockObj = data.getOrDefault("isSelectBlock", false);
        boolean isSelectBlock = (isSelectBlockObj instanceof Boolean) ? (Boolean) isSelectBlockObj : false;

        if (!(isSelectBlockObj instanceof Boolean)) {
            logger.severe("El valor de 'isSelectBlock' no es booleano.");
            return errorResponse("El valor de 'isSelectBlock' debe ser booleano.", 400);
        }

        logger.fine(String.format("conversation_id: %s, date_created: %s, name_history: %s, user_id: %s", convId, dateCreated, nameHistory, userId));

        if (userId == null || userId.isEmpty()) {
            logger.severe("No se proporcionó un userID válido.");
            return errorResponse("Se requiere un userID válido para crear la conversación.", 400);
        }

        try {
            List<Map<String, Object>> allUsers = mongoRepo.getAllUsers();
            boolean userExists = allUsers.stream().anyMatch(user -> userId.equals(user.get("_id")));
            if (!userExists) {
                logger.severe("El userID " + userId + " no existe en la base de datos.");
                return errorResponse("El userID proporcionado no existe en la base de datos.", 404);
            }
        } catch (Exception e) {
            logger.severe("Error al verificar el userID en la base de datos: " + e.getMessage());
            return errorResponse("Error al verificar el userID en la base de datos.", 500);
        }

        boolean isValidContext = validateContext(userMessage);
        logger.fine("Validez del contexto: " + isValidContext);

        if (isValidContext && (nameHistory == null || nameHistory.isEmpty())) {
            nameHistory = nameHistoryGenerator.generateNameHistory(userMessage);
            logger.fine("Nombre de historia generado: " + nameHistory);
        } else {
            if (nameHistory == null || nameHistory.isEmpty()) {
                String[] words = userMessage.trim().split("\\s+");
                nameHistory = String.join(" ", Arrays.copyOfRange(words, 0, Math.min(5, words.length)));
            }
            logger.fine("Nombre de historia generado sin IA: " + nameHistory);
        }

        List<Map<String, Object>> newMessages = new ArrayList<>();
        for (Map<String, Object> msg : messages) {
            if (msg.containsKey("role") && msg.containsKey("content")) {
                Map<String, Object> newMsg = new HashMap<>();
                newMsg.put("id", UUID.randomUUID().toString());
                newMsg.put("role", msg.get("role"));
                newMsg.put("content", msg.get("content"));
                newMessages.add(newMsg);
            }
        }
        logger.fine("Nuevos mensajes creados: " + newMessages);

        int tokensInput = newMessages.stream()
                .filter(msg -> "user".equals(msg.get("role")))
                .mapToInt(msg -> countTokens.countTokens(msg.get("content").toString()))
                .sum();

        int tokensOutput = newMessages.stream()
                .filter(msg -> "assistant".equals(msg.get("role")))
                .mapToInt(msg -> countTokens.countTokens(msg.get("content").toString()))
                .sum();

        int totalTokens = tokensInput + tokensOutput;
        logger.fine(String.format("Tokens de entrada: %d, Tokens de salida: %d, Total tokens: %d", tokensInput, tokensOutput, totalTokens));

        Map<String, Object> validationMessage = new HashMap<>();
        validationMessage.put("id", UUID.randomUUID().toString());
        String validationContent;
        String validationRole = "assistant";
        if ("GenerateTestFunctional".equals(typeContext)) {
            validationContent = isValidContext
                    ? "¡Gracias por el contexto! Ahora necesito que me proporciones la Historia de Usuario (HU) para poder generar los casos de prueba."
                    : "Parece que el contexto proporcionado no está relacionado con la creación de un caso de prueba. Para ayudarte mejor, el contexto proporcionado debe estar relacionado con un negocio, empresa o plan de trabajo. Asegúrate de que el contexto sea claro, específico y relevante para un entorno empresarial.";
        } else if ("GenerateTestFunctionalApi".equals(typeContext)) {
            validationRole = isValidContext ? "system" : "assistant";
            validationContent = isValidContext
                    ? "¡Gracias por el contexto! Ahora necesito que me proporciones detalles técnicos de la API, como los endpoints, parámetros requeridos, códigos de estado y ejemplos de respuesta, para poder generar los casos de prueba."
                    : "El contexto proporcionado no está claro para la generación de casos de prueba para APIs. Por favor, proporciona un contexto técnico que describa la funcionalidad de la API, cómo interactúa con otros sistemas, y cualquier requisito específico necesario para probarla. Asegúrate de que sea claro y relevante.";
        } else if ("GenerateTestFunctionalUi".equals(typeContext)) {
            validationRole = isValidContext ? "system" : "assistant";
            validationContent = isValidContext
                    ? "¡Gracias por el contexto! Ahora necesito que me proporciones capturas de pantalla o descripciones detalladas de la interfaz de usuario, para poder generar los casos de prueba."
                    : "El contexto proporcionado no está claro para la generación de casos de prueba para APIs. Por favor, proporciona un contexto técnico que describa la funcionalidad de la API, cómo interactúa con otros sistemas, y cualquier requisito específico necesario para probarla. Asegúrate de que sea claro y relevante.";
        } else {
            validationContent = "";
        }
        validationMessage.put("role", validationRole);
        validationMessage.put("content", validationContent);
        validationMessage.put("validate", isValidContext);

        List<Map<String, Object>> updatedMessages = new ArrayList<>(newMessages);
        updatedMessages.add(validationMessage);
        logger.fine("Mensajes actualizados: " + updatedMessages);

        int validationTokens = countTokens.countTokens(validationContent);
        tokensOutput += validationTokens;
        totalTokens += validationTokens;

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("_id", convId);
        responseData.put("created", dateCreated);
        responseData.put("conversation_id", convId);
        responseData.put("nameHistory", nameHistory);
        responseData.put("userID", userId);
        responseData.put("step", step);
        responseData.put("isSelectBlock", isSelectBlock);
        responseData.put("selectValue", selectValue);
        responseData.put("contextLevel", contextLevel);
        responseData.put("typeContext", typeContext);
        responseData.put("testType", testType);
        responseData.put("tokens_input", tokensInput);
        responseData.put("tokens_output", tokensOutput);
        responseData.put("tokens", totalTokens);
        responseData.put("max_tokens", maxTokens);
        responseData.put("message", updatedMessages);

        Map<String, Object> messageToSave = new HashMap<>();
        messageToSave.put("_id", convId);
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put("conversationId", convId);
        dataToSave.put("nameHistory", nameHistory);
        dataToSave.put("userID", userId);
        dataToSave.put("step", step);
        dataToSave.put("isSelectBlock", isSelectBlock);
        dataToSave.put("selectValue", selectValue);
        dataToSave.put("typeContext", typeContext);
        dataToSave.put("testType", testType);
        dataToSave.put("tokens_input", tokensInput);
        dataToSave.put("tokens_output", tokensOutput);
        dataToSave.put("tokens", totalTokens);
        dataToSave.put("max_tokens", maxTokens);

        List<Map<String, Object>> filteredMessages = new ArrayList<>();
        for (Map<String, Object> msg : updatedMessages) {
            String role = (String) msg.get("role");
            if ("user".equals(role) || "assistant".equals(role) || "system".equals(role)) {
                filteredMessages.add(msg);
            }
        }
        dataToSave.put("message", filteredMessages);
        messageToSave.put("data", dataToSave);

        mongoRepo.saveConversation(messageToSave);
        logger.info("Mensajes guardados en MongoDB.");

        conversationHistory.put(convId, updatedMessages);
        conversationManager.saveConversation(convId, userMessage, nameHistory, isValidContext);

        logger.info("Conversación guardada exitosamente.");

        Map<String, Object> result = new HashMap<>();
        result.put("data", responseData);
        result.put("status", 200);
        return result;
    }

    public boolean validateContext(String context) {
        logger.fine("Validando contexto: " + context);
        String validationPrompt = promptValidationContext(context);
        logger.fine("Prompt de validación generado: " + validationPrompt);
        Object responseTuple = llm.generateFunctionalTestResponse(validationPrompt, 0.0);

        String response;
        if (responseTuple instanceof Object[] && ((Object[]) responseTuple).length > 0) {
            response = ((Object[]) responseTuple)[0].toString();
        } else {
            response = responseTuple.toString();
        }
        logger.fine("Respuesta del LLM: " + response);
        return "true".equalsIgnoreCase(response.trim());
    }

    public String promptValidationContext(String context) {
        return "El contexto proporcionado debe estar relacionado con un negocio, empresa o plan de trabajo.\n"
                + "Asegúrate de que el contexto sea claro, específico y relevante para un entorno empresarial.\n\n"
                + "Contexto actual: " + context + "\n\n"
                + "Considera los siguientes criterios al evaluar el contexto:\n"
                + "- ¿Describe de manera clara las funciones y responsabilidades relacionadas con el negocio?\n"
                + "- ¿Proporciona detalles sobre los procesos y métodos utilizados?\n"
                + "- ¿Establece conexiones claras entre los objetivos y los resultados esperados?\n"
                + "- ¿Es relevante para las operaciones o estrategias de la empresa?\n\n"
                + "¿Este contexto cumple con los criterios mencionados?\n"
                + "Responde únicamente con 'true' si cumple o 'false' si no cumple, sin ninguna explicación adicional.";
    }

    private Map<String, Object> errorResponse(String message, int status) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", status);
        return error;
    }
}