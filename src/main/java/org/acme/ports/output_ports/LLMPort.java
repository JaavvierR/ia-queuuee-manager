package org.acme.ports.output_ports;

import java.util.Map;

public interface LLMPort {

    /**
     * Genera respuestas para pruebas funcionales.
     * @param prompt El texto de entrada.
     * @param timeout Tiempo de espera opcional en segundos (puede ser null).
     * @return Respuesta generada.
     */
    String generateFunctionalTestResponse(String prompt, Double timeout);

    /**
     * Genera respuestas para pruebas de API.
     * @param prompt El texto de entrada.
     * @param timeout Tiempo de espera opcional en segundos (puede ser null).
     * @return Respuesta generada.
     */
    String generateFunctionalApiTestResponse(String prompt, Double timeout);

    /**
     * Genera respuestas para pruebas de UI.
     * @param prompt El texto de entrada.
     * @param timeout Tiempo de espera opcional en segundos (puede ser null).
     * @return Respuesta generada.
     */
    String generateFunctionalUiTestResponse(String prompt, Double timeout);

    /**
     * Genera respuestas para pruebas de UI con imagen.
     * @param prompt El texto de entrada.
     * @param imageData Datos de la imagen.
     * @param timeout Tiempo de espera opcional en segundos (puede ser null).
     * @return Respuesta generada.
     */
    String generateFunctionalUiTestResponseWithImage(String prompt, Map<String, Object> imageData, Double timeout);

    // Métodos originales para compatibilidad hacia atrás

    /**
     * Genera una respuesta.
     * @param prompt El texto de entrada.
     * @param timeout Tiempo de espera opcional en segundos (puede ser null).
     * @return Respuesta generada.
     */
    String generateResponse(String prompt, Double timeout);

    /**
     * Genera una respuesta con imagen.
     * @param prompt El texto de entrada.
     * @param imageData Datos de la imagen.
     * @param timeout Tiempo de espera opcional en segundos (puede ser null).
     * @return Respuesta generada.
     */
    String generateResponseWithImage(String prompt, Map<String, Object> imageData, Double timeout);
}