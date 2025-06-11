package org.acme.ports.conversation_ports;

public interface ConversationManagementPort {
    /**
     * Inicia una nueva conversación y limpia la caché.
     * @param data Datos para iniciar la conversación.
     * @return Un mapa con la información de la nueva conversación.
     */
    java.util.Map<String, Object> newChat(java.util.Map<String, Object> data);

    /**
     * Guarda la información de la conversación en el historial.
     * @param conversationId ID de la conversación.
     * @param userStory Historia del usuario.
     * @param nameHistory Nombre del historial.
     * @param isValidUserStory Indica si la historia del usuario es válida.
     */
    void saveConversation(String conversationId, String userStory, String nameHistory, boolean isValidUserStory);
}
