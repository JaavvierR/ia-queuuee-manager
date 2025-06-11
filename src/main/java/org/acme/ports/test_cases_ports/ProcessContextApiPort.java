// src/main/java/org/acme/ports/test_cases_ports/process_context_api_port/ProcessContextApiPort.java
package org.acme.ports.test_cases_ports;

import java.util.List;
import java.util.Map;

public interface ProcessContextApiPort {

    /**
     * Procesa el contexto del usuario y genera una respuesta.
     * @param inputData Datos de entrada como un mapa.
     * @param conversationId ID de la conversación.
     * @param userMessage Mensaje del usuario.
     * @param previousMessages Lista de mensajes previos.
     * @return Una tupla (puedes usar un objeto personalizado o Map.Entry, o crear una clase ResponseTuple).
     */
    Object processContext(
        Map<String, Object> inputData,
        String conversationId,
        String userMessage,
        List<Object> previousMessages
    );

    /**
     * Valida el contexto proporcionado por el usuario.
     * @param context Contexto a validar.
     * @return true si es válido, false en caso contrario.
     */
    boolean validateContext(String context);

    /**
     * Genera un prompt para validar el contexto.
     * @param context Contexto a usar en el prompt.
     * @return El prompt generado.
     */
    String promptValidationContext(String context);
}
