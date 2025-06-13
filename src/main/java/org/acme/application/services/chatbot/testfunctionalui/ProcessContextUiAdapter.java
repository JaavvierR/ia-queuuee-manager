package org.acme.application.services.chatbot.testfunctionalui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.acme.ports.test_cases_ports.ProcessContextUiPort;
import org.acme.ports.output_ports.MongoDBPort;
import org.acme.ports.conversation_ports.NameHistoryGeneratorPort;
import org.acme.ports.conversation_ports.ConversationManagementPort;
import org.acme.ports.output_ports.LLMPort;
import org.acme.ports.test_cases_ports.CountTokensPort;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

public class ProcessContextUiAdapter extends ProcessContextUiPort {
    @Override
    public Map<String, Object> processContext(Object input) {
        // You can delegate to the existing processContext(Map, ...) if possible
        if (input instanceof Map) {
            // Provide dummy/default values for the other parameters if needed
            @SuppressWarnings("unchecked")
            Map<String, Object> inputMap = (Map<String, Object>) input;
            return processContext(inputMap, null, null, new ArrayList<>());
        }
        throw new UnsupportedOperationException("Input type not supported for processContext(Object)");
    }

    @Override
    public String generateImageDescription(BufferedImage image) {
        // Dummy implementation, you should replace with actual logic if needed
        return "Descripción de imagen no implementada.";
    }
    private final LLMPort llm;
    private final MongoDBPort mongoRepo;
    private final NameHistoryGeneratorPort nameHistoryGenerator;
    private final ConversationManagementPort conversationManager;
    private final CountTokensPort countTokens;
    private final String apiStorageUrl;
    private final Logger logger = LoggerFactory.getLogger(ProcessContextUiAdapter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProcessContextUiAdapter(
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
        this.apiStorageUrl = System.getenv().getOrDefault("API_STORAGE_URL", "http://qava-cn-ai-storage:6090/upload");
        logger.debug("Conexion establecida a API de almacenamiento: {}", this.apiStorageUrl);
    }

    private boolean isValidBase64Image(String dataStr, String mediaType) {
        if (dataStr == null || dataStr.isEmpty()) return false;
        try {
            Map<String, List<String>> imageSignatures = new HashMap<>();
            imageSignatures.put("jpeg", Arrays.asList("/9j/", "/9k/", "/9l/", "/9m/", "/9n/", "/9o/", "/9p/", "/9q/", "/9r/", "/9s/", "/9t/", "/9u/", "/9v/", "/9w/", "/9x/", "/9y/", "/9z/"));
            imageSignatures.put("png", Collections.singletonList("iVBORw0KGgo"));
            imageSignatures.put("gif", Collections.singletonList("R0lGOD"));
            imageSignatures.put("bmp", Arrays.asList("Qk0", "Qk1", "Qk4", "Qk5"));
            imageSignatures.put("webp", Collections.singletonList("UklGR"));

            for (Map.Entry<String, List<String>> entry : imageSignatures.entrySet()) {
                for (String sig : entry.getValue()) {
                    if (dataStr.startsWith(sig)) {
                        logger.debug("Imagen detectada como {} por firma: {}", entry.getKey().toUpperCase(), sig);
                        return true;
                    }
                }
            }

            if (mediaType != null) {
                String mediaLower = mediaType.toLowerCase();
                if (mediaLower.contains("png") && dataStr.startsWith("iVBORw0KGgo")) return true;
                if ((mediaLower.contains("jpeg") || mediaLower.contains("jpg")) && imageSignatures.get("jpeg").stream().anyMatch(dataStr::startsWith)) return true;
                if (mediaLower.contains("gif") && dataStr.startsWith("R0lGOD")) return true;
                if (mediaLower.contains("bmp") && imageSignatures.get("bmp").stream().anyMatch(dataStr::startsWith)) return true;
                if (mediaLower.contains("webp") && dataStr.startsWith("UklGR")) return true;
            }

            byte[] decoded = Base64.decodeBase64(dataStr);
            if (decoded.length > 10) {
                byte[] header = Arrays.copyOfRange(decoded, 0, 10);
                if (startsWith(header, new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) return true; // PNG
                if (startsWith(header, new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF})) return true; // JPEG
                if (startsWith(header, "GIF8".getBytes())) return true; // GIF
                if (startsWith(header, "BM".getBytes())) return true; // BMP
                if (startsWith(header, "RIFF".getBytes()) && decoded.length > 12 && Arrays.equals(Arrays.copyOfRange(decoded, 8, 12), "WEBP".getBytes())) return true; // WebP
            }
        } catch (Exception e) {
            logger.error("Error en validación de imagen base64: {}", e.getMessage());
            return false;
        }
        return false;
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    private ImageFormatInfo getImageFormatAndExtension(String dataStr, String mediaType) {
        Map<String, ImageFormatInfo> formatMapping = new HashMap<>();
        formatMapping.put("iVBORw0KGgo", new ImageFormatInfo(".png", "image/png"));
        formatMapping.put("/9j/", new ImageFormatInfo(".jpg", "image/jpeg"));
        formatMapping.put("/9k/", new ImageFormatInfo(".jpg", "image/jpeg"));
        formatMapping.put("R0lGOD", new ImageFormatInfo(".gif", "image/gif"));
        formatMapping.put("Qk0", new ImageFormatInfo(".bmp", "image/bmp"));
        formatMapping.put("Qk1", new ImageFormatInfo(".bmp", "image/bmp"));
        formatMapping.put("UklGR", new ImageFormatInfo(".webp", "image/webp"));

        for (Map.Entry<String, ImageFormatInfo> entry : formatMapping.entrySet()) {
            if (dataStr.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        if (mediaType != null) {
            String mediaLower = mediaType.toLowerCase();
            if (mediaLower.contains("png")) return new ImageFormatInfo(".png", "image/png");
            if (mediaLower.contains("jpeg") || mediaLower.contains("jpg")) return new ImageFormatInfo(".jpg", "image/jpeg");
            if (mediaLower.contains("gif")) return new ImageFormatInfo(".gif", "image/gif");
            if (mediaLower.contains("bmp")) return new ImageFormatInfo(".bmp", "image/bmp");
            if (mediaLower.contains("webp")) return new ImageFormatInfo(".webp", "image/webp");
        }
        return new ImageFormatInfo(".jpg", "image/jpeg");
    }

    public Map<String, Object> processContext(Map<String, Object> inputData, String conversationId, String userMessage, List<Map<String, Object>> previousMessages) {
        logger.debug("Procesando test_type 'context' para UI...");
        Object dataObj = inputData.get("data");
        if (!(dataObj instanceof Map)) {
            logger.error("'data' field is missing or not a Map.");
            return errorResponse("'data' field is missing or not a Map.", 400);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) dataObj;
        String convId = data.getOrDefault("conversation_id", UUID.randomUUID().toString()).toString();
        String dateCreated = data.getOrDefault("created", Instant.now().toString()).toString();
        List<Map<String, Object>> messages;
        Object messagesObj = data.getOrDefault("message", new ArrayList<>());
        if (messagesObj instanceof List) {
            List<?> tempList = (List<?>) messagesObj;
            boolean allMaps = tempList.stream().allMatch(item -> item instanceof Map);
            if (allMaps) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> safeList = (List<Map<String, Object>>) messagesObj;
                messages = safeList;
            } else {
                messages = new ArrayList<>();
            }
        } else {
            messages = new ArrayList<>();
        }
        String nameHistory = (String) data.get("nameHistory");
        Object step = data.get("step");
        Object selectValue = data.get("selectValue");
        Object testType = data.get("testType");
        Object typeContext = data.get("typeContext");
        String userId = (String) data.get("userID");
        Object isSelectBlockObj = data.getOrDefault("isSelectBlock", false);
        boolean isSelectBlock = isSelectBlockObj instanceof Boolean ? (Boolean) isSelectBlockObj : Boolean.parseBoolean(isSelectBlockObj.toString());
        Object contextLevel = data.get("contextLevel");

        if (!isSelectBlockObj.getClass().equals(Boolean.class)) {
            logger.error("El valor de 'isSelectBlock' no es booleano.");
            return errorResponse("El valor de 'isSelectBlock' debe ser booleano.", 400);
        }

        if (userId == null || userId.isEmpty()) {
            logger.error("No se proporcionó un userID válido.");
            return errorResponse("Se requiere un userID válido para crear la conversación.", 400);
        }

        try {
            List<Map<String, Object>> allUsers = mongoRepo.getAllUsers();
            boolean userExists = allUsers.stream().anyMatch(u -> userId.equals(u.get("_id")));
            if (!userExists) {
                logger.error("El userID {} no existe en la base de datos.", userId);
                return errorResponse("El userID proporcionado no existe en la base de datos.", 404);
            }
        } catch (Exception e) {
            logger.error("Error al verificar el userID: {}", e.getMessage());
            return errorResponse("Error al verificar el userID en la base de datos.", 500);
        }

        byte[] imageData = null;
        Map<String, Object> fileUploadInfo = null;

        try {
            for (int i = messages.size() - 1; i >= 0; i--) {
                Map<String, Object> msg = messages.get(i);
                if ("user".equals(msg.get("role"))) {
                    Object contentObj = msg.get("content");
                    Map<String, Object> content;
                    if (contentObj instanceof String) {
                        content = objectMapper.readValue((String) contentObj, new TypeReference<Map<String, Object>>() {});
                    } else {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tempContent = (Map<String, Object>) contentObj;
                        content = tempContent;
                    }
                    if ("image".equals(content.get("file_type")) && content.containsKey("source")) {
                        Object sourceObj = content.get("source");
                        Map<String, Object> source;
                        if (sourceObj instanceof String) {
                            source = objectMapper.readValue((String) sourceObj, new TypeReference<Map<String, Object>>() {});
                        } else {
                            if (sourceObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> tempSource = (Map<String, Object>) sourceObj;
                                source = tempSource;
                            } else {
                                throw new IllegalArgumentException("source is not a Map<String, Object>");
                            }
                        }
                        String mediaType = (String) source.getOrDefault("media_type", "");
                        if ("base64".equals(source.get("type"))) {
                            String dataStr = (String) source.getOrDefault("data", "");
                            if (!dataStr.isEmpty() && isValidBase64Image(dataStr, mediaType)) {
                                ImageFormatInfo formatInfo = getImageFormatAndExtension(dataStr, mediaType);
                                imageData = Base64.decodeBase64(dataStr);
                                String tempDir = System.getProperty("java.io.tmpdir");
                                String originalFilename = (String) source.getOrDefault("file_name", "");
                                String imageFilename = originalFilename.isEmpty() ? UUID.randomUUID() + formatInfo.extension : originalFilename;
                                File tempFile = new File(tempDir, imageFilename);
                                Files.write(tempFile.toPath(), imageData);

                                Map<String, Object> uploadResult = uploadImage(tempFile, imageFilename, formatInfo.mimeType);
                                if (uploadResult != null) {
                                    fileUploadInfo = new HashMap<>();
                                    fileUploadInfo.put("file_name", imageFilename);
                                    fileUploadInfo.put("file_size", imageData.length);
                                    fileUploadInfo.put("file_url", uploadResult.get("file_url"));
                                } else {
                                    throw new Exception("Error al subir la imagen");
                                }
                                tempFile.delete();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error al procesar la imagen: {}", e.getMessage());
            return errorResponse("Error al procesar la imagen: " + e.getMessage(), 400);
        }

        if (imageData == null) {
            logger.error("No se proporcionó ninguna imagen o no se pudo extraer del mensaje.");
            return errorResponse("Se requiere una imagen para procesar el contexto UI.", 400);
        }

        String processedContent;
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String formatName = img != null && img.getType() == BufferedImage.TYPE_INT_ARGB ? "png" : "jpg";
            ImageIO.write(img, formatName, baos);
            Map<String, Object> geminiImage = new HashMap<>();
            geminiImage.put("mime_type", "image/" + formatName);
            geminiImage.put("data", baos.toByteArray());
            processedContent = generateImageDescription(geminiImage);
        } catch (Exception e) {
            logger.error("Error al procesar la imagen: {}", e.getMessage());
            return errorResponse("Error al procesar la imagen: " + e.getMessage(), 500);
        }

        boolean isValidContext;
        try {
            isValidContext = validateUiContext(processedContent);
        } catch (Exception e) {
            logger.error("Error al validar el contexto UI: {}", e.getMessage());
            return errorResponse("Error al validar el contexto UI: " + e.getMessage(), 500);
        }

        if (nameHistory == null || nameHistory.isEmpty()) {
            try {
                if (isValidContext) {
                    nameHistory = nameHistoryGenerator.generateNameHistory(processedContent);
                } else {
                    nameHistory = Arrays.stream(processedContent.trim().split("\\s+")).limit(5).reduce((a, b) -> a + " " + b).orElse("");
                }
            } catch (Exception e) {
                logger.error("Error al generar nombre de historia: {}", e.getMessage());
                return errorResponse("Error al generar nombre de historia: " + e.getMessage(), 500);
            }
        }

        List<Map<String, Object>> newMessages = new ArrayList<>();
        try {
            for (int i = 0; i < messages.size(); i++) {
                Map<String, Object> msg = messages.get(i);
                if (!("user".equals(msg.get("role")) && i == messages.size() - 1)) {
                    Map<String, Object> newMsg = new HashMap<>(msg);
                    if (!newMsg.containsKey("id")) newMsg.put("id", UUID.randomUUID().toString());
                    newMessages.add(newMsg);
                }
            }
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("id", UUID.randomUUID().toString());
            userMsg.put("role", "user");
            Map<String, Object> content = new HashMap<>();
            content.put("file_name", fileUploadInfo.get("file_name"));
            content.put("file_size", fileUploadInfo.get("file_size"));
            content.put("file_type", "image");
            content.put("file_url", fileUploadInfo.get("file_url"));
            Map<String, Object> source = new HashMap<>();
            source.put("data", processedContent);
            source.put("media_type", "image/jpeg");
            source.put("status", "success");
            source.put("type", "base64");
            content.put("source", source);
            userMsg.put("content", content);
            newMessages.add(userMsg);

            Map<String, Object> validationMsg = new HashMap<>();
            validationMsg.put("id", UUID.randomUUID().toString());
            validationMsg.put("role", "assistant");
            validationMsg.put("content", "¡Gracias por el contexto! Ahora necesito que me proporciones la Historia de Usuario (HU) para poder generar los casos de prueba.");
            validationMsg.put("isHu", true);
            validationMsg.put("validate", isValidContext);
            newMessages.add(validationMsg);
        } catch (Exception e) {
            logger.error("Error al crear nuevos mensajes: {}", e.getMessage());
            return errorResponse("Error al crear nuevos mensajes: " + e.getMessage(), 500);
        }

        int tokensInput, tokensOutput, totalTokens;
        try {
            tokensInput = countTokens.countTokens(processedContent);
            tokensOutput = countTokens.countTokens("¡Gracias por el contexto! Ahora necesito que me proporciones la Historia de Usuario (HU) para poder generar los casos de prueba.");
            totalTokens = tokensInput + tokensOutput;
        } catch (Exception e) {
            logger.error("Error al contar tokens: {}", e.getMessage());
            return errorResponse("Error al contar tokens: " + e.getMessage(), 500);
        }

        int maxTokens = 3500;
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
        responseData.put("message", newMessages);

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
        dataToSave.put("message", newMessages);
        messageToSave.put("data", dataToSave);

        try {
            mongoRepo.saveConversation(messageToSave);
            logger.info("Conversaciones guardadas en MongoDB.");
        } catch (Exception e) {
            logger.error("Error al guardar en MongoDB: {}", e.getMessage());
            return errorResponse("Error al guardar en MongoDB: " + e.getMessage(), 500);
        }

        conversationManager.saveConversation(convId, processedContent, nameHistory, isValidContext);

        Map<String, Object> result = new HashMap<>();
        result.put("data", responseData);
        result.put("status", 200);
        return result;
    }

    private Map<String, Object> uploadImage(File file, String fileName, String mimeType) throws IOException {
        String boundary = UUID.randomUUID().toString();
        HttpURLConnection connection = (HttpURLConnection) java.net.URI.create(apiStorageUrl).toURL().openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = connection.getOutputStream()) {
            String filePartHeader = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                    "Content-Type: " + mimeType + "\r\n\r\n";
            output.write(filePartHeader.getBytes());
            Files.copy(file.toPath(), output);
            output.write("\r\n".getBytes());

            String dataPart = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file_name\"\r\n\r\n" +
                    fileName + "\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file_type\"\r\n\r\n" +
                    "image\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"bucket_name\"\r\n\r\n" +
                    "document-bucket\r\n" +
                    "--" + boundary + "--\r\n";
            output.write(dataPart.getBytes());
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (InputStream is = connection.getInputStream()) {
                return objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            }
        } else {
            try (InputStream es = connection.getErrorStream()) {
                String error = es != null ? new String(es.readAllBytes()) : "";
                throw new IOException("Error al subir la imagen: " + error);
            }
        }
    }

    private String generateImageDescription(Map<String, Object> imageData) throws Exception {
        String promptDescripcion = "Describe detalladamente lo que ves en la imagen, responde en español. Solo la desripcion directa, sin comentarios adicionales.";
        String response = llm.generateFunctionalUiTestResponseWithImage(promptDescripcion, imageData, null);
        if (response != null && !response.isEmpty()) {
            return response;
        } else {
            throw new Exception("Gemini no generó ninguna descripción para la imagen");
        }
    }

    @Override
    public boolean validateUiContext(String descripcion) {
        String validationPrompt = "---\n" + descripcion + "\n---\nResponde solo con 'Sí' o 'No'. Un contexto UI válido debe describir un componente visual claro y estructurado.";
        String response = llm.generateFunctionalApiTestResponse(validationPrompt, null);
        return "sí".equalsIgnoreCase(response.trim());
    }

    private Map<String, Object> errorResponse(String message, int code) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", code);
        return error;
    }

    private static class ImageFormatInfo {
        String extension;
        String mimeType;

        ImageFormatInfo(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
    }
}
