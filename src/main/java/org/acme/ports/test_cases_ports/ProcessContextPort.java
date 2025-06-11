package org.acme.ports.test_cases_ports;

import java.util.List;
import java.util.Map;

public interface ProcessContextPort {
    /**
     * Procesa el contexto del usuario y genera una respuesta.
     * 
     * @param inputData         Datos de entrada como un Map
     * @param conversationId    ID de la conversación
     * @param userMessage       Mensaje del usuario
     * @param previousMessages  Lista de mensajes previos
     * @return                  Un objeto ResultadoContexto (deberás definir esta clase)
     */
    java.util.Map<String, Object> processContext(
        Map<String, Object> inputData,
        String conversationId,
        String userMessage,
        List<String> previousMessages
    );

    /**
     * Valida el contexto proporcionado por el usuario.
     * 
     * @param context   Contexto a validar
     * @return          true si es válido, false en caso contrario
     */
    boolean validateContext(String context);

    /**
     * Genera un prompt para validar el contexto.
     * 
     * @param context   Contexto a validar
     * @return          Prompt generado
     */
    String promptValidationContext(String context);

    // Debes crear la clase ResultadoContexto según tus necesidades.
}
