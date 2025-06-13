package org.acme.infrastructure.providers;

import org.acme.application.ports.output.common.TextCompletionPort;
import org.acme.infrastructure.providers.qualifers.AIProviderQualifiers.OpenAI;
import org.acme.infrastructure.providers.qualifers.AIProviderQualifiers.Gemini;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.inject.Default;

import io.quarkus.logging.Log;

@ApplicationScoped
public class TextCompletionPortProvider {

    @Inject
    @OpenAI
    TextCompletionPort openAIAdapter;

    @Inject
    @Gemini
    TextCompletionPort geminiAdapter;

    @ConfigProperty(name = "flagger.model")
    String model;

    @Produces
    @Default
    @ApplicationScoped
    public TextCompletionPort produceTextCompletionPort() {
        switch (model.toLowerCase()) {
            case "openai":
                Log.info("flagger.model.openai");
                return openAIAdapter;
            case "gemini":
                Log.info("flagger.model.gemini");
                return geminiAdapter;
            default:
                throw new IllegalArgumentException("Modelo no soportado: " + model);
        }
    }
}