package org.acme.infrastructure.adapters.output.common.gemini;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.acme.infrastructure.providers.qualifers.AIProviderQualifiers.Gemini;

@ApplicationScoped
@Gemini
public class GeminiConfig {
    @ConfigProperty(name = "gemini.api.key")
    private String apiKey;

    @ConfigProperty(name = "gemini.api.url", defaultValue = "https://generativelanguage.googleapis.com/v1beta/models")
    private String baseApiUrl;

    @ConfigProperty(name = "gemini.model.name", defaultValue = "gemini-2.0-flash")
    private String modelName;

    @ConfigProperty(name = "gemini.default.temperature", defaultValue = "0.7")
    private double defaultTemperature;

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseApiUrl() {
        return baseApiUrl;
    }

    public String getModelName() {
        return modelName;
    }


    public double getDefaultTemperature() {
        return defaultTemperature;
    }

    public String getApiUrl() {
        return baseApiUrl + "/" + modelName + ":generateContent?key=" + apiKey;
    }
}