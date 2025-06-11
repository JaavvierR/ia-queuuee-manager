package org.acme.ports.test_cases_ports;

import java.util.Map;

public interface PostmanCollectionPort {
    /**
     * Extrae el contenido CSV de un texto.
     *
     * @param text Texto que contiene el contenido CSV.
     * @return Contenido CSV extraído.
     */
    String extractCsvContent(String text);

    /**
     * Genera una colección de Postman a partir de un conversation_id.
     *
     * @param conversationId Identificador de la conversación.
     * @return Colección de Postman en formato JSON.
     */
    Map<String, Object> generatePostmanCollection(String conversationId);

    /**
     * Genera el prompt para Postman a partir de los casos de prueba.
     *
     * @param testCases Casos de prueba.
     * @return Prompt generado.
     */
    String promptGeneratePostman(String testCases);
}
