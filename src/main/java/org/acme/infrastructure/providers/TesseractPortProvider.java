package org.acme.infrastructure.providers;

import org.acme.application.ports.output.textextraction.DocumentProcessorPort;
import org.acme.infrastructure.providers.qualifers.DocumentProcessorQualifiers.PythonTesseract;
import org.acme.infrastructure.providers.qualifers.DocumentProcessorQualifiers.JavaTesseract;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.inject.Default;

import io.quarkus.logging.Log;

public class TesseractPortProvider {
    @Inject
    @PythonTesseract 
    DocumentProcessorPort pythonDocumentProcessorAdapter;

    @Inject
    @JavaTesseract 
    DocumentProcessorPort documentProcessorAdapter;

    @ConfigProperty(name = "flagger.tesseract")
    String tesseract_provider;

    @Produces
    @Default
    @ApplicationScoped
    public DocumentProcessorPort produceDocumentProcessorPort(){
        switch (tesseract_provider.toLowerCase()) {
            case "python":
                Log.info("flagger.tesseract.python");
                return pythonDocumentProcessorAdapter;
            case "java":
                Log.info("flagger.tesseract.java");
                return documentProcessorAdapter;
            default:
                throw new IllegalArgumentException("Provider de tesseract no soportado" + tesseract_provider);
        }
    }

}
