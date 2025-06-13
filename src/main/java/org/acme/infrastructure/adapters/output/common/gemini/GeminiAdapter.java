package org.acme.infrastructure.adapters.output.common.gemini;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.acme.application.ports.output.common.TextCompletionPort;

import org.acme.infrastructure.providers.qualifers.AIProviderQualifiers.Gemini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ApplicationScoped
@Gemini
public class GeminiAdapter implements TextCompletionPort {
    private static final Logger LOG = Logger.getLogger(GeminiAdapter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ThreadLocal<String> currentExtractedText = new ThreadLocal<>();
    
    private final GeminiConfig config;
    
    private long startTime = 0;
    private long pauseTime = 0;
    private boolean timerRunning = false;
    private double elapsedTime = 0;
    
    @Inject
    public GeminiAdapter(@Gemini GeminiConfig config) {
        this.config = config;
    }
    
    @PostConstruct
    void init() {
        LOG.info("⚡ Cargando API Key de Gemini: " + (config.getApiKey() != null && !config.getApiKey().isEmpty() ? "OK" : "FALLO"));
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new RuntimeException("⚠️ API Key de Gemini no configurada o vacía");
        }
    }

    @Override
    public String sendMessage(String message) {
        LOG.info("sendMessage called with: " + message);
        return "sendMessage response placeholder";
    }

    @Override
    public String generateResponse(String prompt) {  
        LOG.info("generateResponse called with: " + prompt);
        return "generateResponse response placeholder";
    }

    @Override
    public String extractEntities(String text) {    
        LOG.info("extractEntities called with: " + text);
        return "extractEntities response placeholder";
    }
    
   

    
    private void createSection(ObjectNode sections, String sectionName, JsonNode source, String sourceKey, 
                              Object defaultContent, double defaultScore) {
        ObjectNode section = sections.putObject(sectionName);
        
        ArrayNode contentNode = MAPPER.createArrayNode();
        if (source != null && source.has(sourceKey) && source.get(sourceKey).isArray()) {
            JsonNode sourceContent = source.get(sourceKey);
            for (JsonNode item : sourceContent) {
                if (item.isObject()) {
                    contentNode.add(item);
                } else if (item.isTextual()) {
                    contentNode.add(item.asText());
                }
            }
        } else if (defaultContent instanceof List) {
            for (Object item : (List<?>) defaultContent) {
                if (item instanceof Map) {
                    try {
                        contentNode.add(MAPPER.valueToTree(item));
                    } catch (Exception e) {
                        LOG.error("Error al convertir mapa a JSON: " + e.getMessage());
                    }
                } else if (item instanceof String) {
                    contentNode.add((String) item);
                }
            }
        }
        
        section.set("content", contentNode);
        int count = contentNode.size();
        section.put("count", count);
        
        String note = count > 0 
                ? "Usted obtuvo " + defaultScore + " puntos porque tiene " + count + " " + sectionName + "."
                : "El puntaje es de 0 porque no se ha encontrado ningún tipo de " + sectionName + ".";
        
        section.put("note", note);
        section.put("score", defaultScore);
        section.put("validate", true);
    }
    
    private void createEmptySection(ObjectNode sections, String sectionName, String note) {
        ObjectNode section = sections.putObject(sectionName);
        section.set("content", MAPPER.createArrayNode());
        section.put("count", 0);
        section.put("note", note);
        section.put("score", 0);
        section.put("validate", true);
    }
    
    private List<Map<String, String>> createDefaultEducation() {
        List<Map<String, String>> education = new ArrayList<>();
        
        Map<String, String> edu1 = new HashMap<>();
        edu1.put("Country", "LIMA");
        edu1.put("date", "2025");
        edu1.put("degree", "Nivel Profesional Técnico");
        edu1.put("institution", "SERVICIO NACIONAL DE ADIESTRAMIENTO EN TRABAJO INDUSTRIAL");
        
        Map<String, String> edu2 = new HashMap<>();
        edu2.put("Country", "CALLAO");
        edu2.put("date", "");
        edu2.put("degree", "");
        edu2.put("institution", "C.EP. VILLA EL SALADOR");
        
        education.add(edu1);
        education.add(edu2);
        return education;
    }
    
    private List<String> createDefaultSoftSkills() {
        List<String> skills = new ArrayList<>();
        skills.add("Dedicación");
        skills.add("Apoyo");
        skills.add("Confianza");
        return skills;
    }
    
    private List<String> createDefaultTechStack() {
        List<String> tech = new ArrayList<>();
        tech.add("Inteligencia Artificial");
        return tech;
    }
    
    private List<Map<String, String>> createDefaultWorkExperience() {
        List<Map<String, String>> workExp = new ArrayList<>();
        
        Map<String, String> exp = new HashMap<>();
        exp.put("Country", "LIMA");
        exp.put("company", "SERVICIO NACIONAL DE ADIESTRAMIENTO EN TRABAJO INDUSTRIAL");
        exp.put("date", "2025");
        exp.put("job_title", "Ingenieria de Software");
        
        workExp.add(exp);
        return workExp;
    }
    
    private String getNodeValueOrDefault(JsonNode node, String key, String defaultValue) {
        if (node != null && node.has(key) && node.get(key).isTextual()) {
            return node.get(key).asText();
        }
        return defaultValue;
    }
    
    private int getNodeValueOrDefault(JsonNode node, String key, int defaultValue) {
        if (node != null && node.has(key) && node.get(key).isInt()) {
            return node.get(key).asInt();
        }
        return defaultValue;
    }
    
    
    
    @Override
    public String getModelName() {
        return config.getModelName();
    }
    
    @Override
    public void startTimer() {
        if (!timerRunning) {
            startTime = System.currentTimeMillis();
            timerRunning = true;
        }
    }
    
    @Override
    public void pauseTimer() {
        if (timerRunning) {
            pauseTime = System.currentTimeMillis();
            timerRunning = false;
            elapsedTime += (pauseTime - startTime) / 1000.0;
        }
    }
    
    @Override
    public double getElapsedTime() {
        if (timerRunning) {
            long currentTime = System.currentTimeMillis();
            return elapsedTime + (currentTime - startTime) / 1000.0;
        } else {
            return elapsedTime;
        }
    }
}