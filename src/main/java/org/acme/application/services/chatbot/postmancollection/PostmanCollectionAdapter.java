package org.acme.application.services.chatbot.postmancollection;

import com.fasterxml.jackson.databind.ObjectMapper;



import org.acme.ports.output_ports.LLMPort;
import org.acme.ports.test_cases_ports.PostmanCollectionPort;
import org.acme.ports.output_ports.MongoDBPort;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostmanCollectionAdapter implements PostmanCollectionPort {
    private final MongoDBPort mongoRepo;
    private final LLMPort llm;
    private final Logger logger = Logger.getLogger(PostmanCollectionAdapter.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PostmanCollectionAdapter(MongoDBPort mongoRepo, LLMPort llm) {
        this.mongoRepo = mongoRepo;
        this.llm = llm;
    }

    @Override
    public String extractCsvContent(String text) {
        logger.fine("Extracting CSV content from text: " + text);
        Pattern pattern = Pattern.compile("\\*\\*csv\\*\\*(.*?)\\*\\*csv\\*\\*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        String testCases = matcher.find() ? matcher.group(1).trim() : null;
        logger.fine("Extracted CSV content: " + testCases);
        return testCases;
    }

    @Override
    public Map<String, Object> generatePostmanCollection(String conversationId) {
        try {
            logger.fine("Generating Postman collection for conversation ID: " + conversationId);
            Map<String, Object> conversation = mongoRepo.getConversationById(conversationId);

            if (conversation == null) {
                throw new IllegalArgumentException("Conversación no encontrada para el ID: " + conversationId);
            }

            Object dataObj = conversation.get("data");
            Map<String, Object> data;
            if (dataObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tempData = (Map<String, Object>) dataObj;
                data = tempData;
            } else {
                throw new IllegalArgumentException("El campo 'data' no tiene el formato esperado de Map<String, Object>");
            }
            if (data == null || !data.containsKey("message")) {
                throw new IllegalArgumentException("No se encontraron mensajes en la conversación con ID: " + conversationId);
            }

            Object messagesObj = data.get("message");
            if (!(messagesObj instanceof java.util.List) || ((java.util.List<?>) messagesObj).isEmpty()) {
                throw new IllegalArgumentException("No se encontraron mensajes en la conversación con ID: " + conversationId);
            }

            java.util.List<?> messages = (java.util.List<?>) messagesObj;
            Object lastMsgObj = messages.get(messages.size() - 1);
            Map<String, Object> lastMessage;
            if (lastMsgObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) lastMsgObj;
                lastMessage = temp;
            } else {
                throw new IllegalArgumentException("El último mensaje no tiene el formato esperado de Map<String, Object>");
            }
            String testCases = (String) lastMessage.get("content");
            logger.fine("Último mensaje de la conversación (test_cases): " + testCases);

            String prompt = promptGeneratePostman(testCases);
            String response = llm.generateResponse(prompt, 0.7);
            logger.fine("Respuesta completa: " + response);

            // Extraer JSON entre ```json ... ```
            Pattern jsonPattern = Pattern.compile("```json\\n(.*?)\\n```", Pattern.DOTALL);
            Matcher jsonMatcher = jsonPattern.matcher(response);
            String jsonContent;
            if (jsonMatcher.find()) {
                jsonContent = jsonMatcher.group(1).trim();
                logger.fine("Contenido JSON extraído: " + jsonContent);
            } else {
                logger.warning("No se encontró bloque ```json, intentando parsear respuesta completa");
                jsonContent = response.trim();
            }

            // Parsear JSON
            Map<String, Object> postmanCollection;
            try {
                postmanCollection = objectMapper.readValue(jsonContent, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                logger.severe("No se pudo parsear el JSON: " + jsonContent);
                throw new RuntimeException("No se pudo extraer un JSON válido de la respuesta", e);
            }

            logger.info("Colección Postman generada exitosamente para el ID: " + conversationId);
            return postmanCollection;

        } catch (Exception e) {
            logger.severe("Error generando colección Postman: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String promptGeneratePostman(String testCases) {
        return String.format("""
        Eres un experto en la generación de colecciones Postman en formato JSON versión 2.1. Tu tarea es crear una colección Postman completamente funcional basada en los datos proporcionados. Asegúrate de cumplir con los siguientes requisitos estrictos.

        ### Especificaciones de la colección:
        1. **Objeto "info"**:
        - Debe incluir:
            - Encabezado "info"**: 
                - `_postman_id`: Un UUID único generado dinámicamente.
                - `name`: Un nombre descriptivo para identificar la colección.
                - `schema`: `"https://schema.getpostman.com/json/collection/v2.1.0/collection.json"`.

        2. **Estructura del array "item"**:
        - Cada elemento del array representa un endpoint o caso de prueba.
        - Asegúrate de incluir:
            - **Método HTTP**: GET, POST, PUT, DELETE, etc.
            - **URL completa**: Incluye protocolo (http/https), host, puerto (si aplica) y path definido en el test case.
            - **Headers**: Proporciona encabezados relevantes, como `Content-Type` y `Authorization`, según se requiera.
            - **Body**: En métodos como POST y PUT, incluye un ejemplo claro en formato JSON o según la especificación indicada.
            - **Path variables y query parameters**: Define estos elementos cuando sean necesarios, siguiendo la estructura de Postman.
            - **Ejemplo de respuesta (opcional)**: Incluye un ejemplo representativo de response JSON si está disponible.

        3. **Organización y estructura**:
        - Agrupa los endpoints en carpetas lógicas según categorías o funcionalidades descritas en los test cases para facilitar la navegación.
        - Mantén una jerarquía clara y una nomenclatura consistente para las carpetas y nombres de los elementos.

        4. **Compatibilidad y validación**:
        - La estructura del JSON debe ser completamente válida y compatible con el esquema oficial de Postman v2.1.0.
        - Asegúrate de que todos los elementos tengan las propiedades requeridas y sigan las reglas de la especificación.

        ### Contexto para la generación:
        Usa la información contenida en la variable %s como base para generar la colección. Este CSV contiene:
        - Rutas y métodos HTTP para los endpoints.
        - Detalles como parámetros, headers, cuerpos de request, etc.
        - Cualquier otra información necesaria para crear la colección.

        ### Instrucciones finales:
        - Devuelve únicamente el JSON de la colección en el formato exacto de Postman v2.1.0.
        - No incluyas explicaciones, comentarios ni texto adicional en la respuesta.
        - La respuesta debe ser completamente utilizable al importar en Postman.

        ###
        """, testCases);
    }
}