package org.acme.ports.conversation_ports;

public interface NameHistoryGeneratorPort {
    /**
     * Genera un nombre basado en el contexto proporcionado.
     *
     * @param contextData Datos de contexto para generar el nombre.
     * @return El nombre generado basado en el contexto.
     */
    String generateNameHistory(String contextData);
}
