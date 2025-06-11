package org.acme.ports.output_ports;

import java.util.List;
import java.util.Map;

public interface MongoDBPort {
    /**
     * Guarda una conversacion en MongoDB.
     * @param conversation Conversación a guardar.
     */
    void saveConversation(Map<String, Object> conversation);

    /**
     * Recupera una conversacion de MongoDB por su ID.
     * @param conversationId ID de la conversación.
     * @return Conversación encontrada.
     */
    Map<String, Object> getConversationById(String conversationId);

    /**
     * Recupera todos las conversacions de MongoDB.
     * @return Lista de conversaciones.
     */
    List<Map<String, Object>> getAllConversations();

    /**
     * Recupera las conversacions de contexto para una conversation_id dado.
     * @param conversationId ID de la conversación.
     * @return Lista de conversaciones de contexto.
     */
    List<Map<String, Object>> getContextConversations(String conversationId);

    /**
     * Actualiza una conversacion en MongoDB.
     * @param conversation Conversación a actualizar.
     */
    void updateConversationContext(Map<String, Object> conversation);

    /**
     * Actualiza los campos de user story de una conversación.
     * @param conversationId ID de la conversación.
     * @param updateFields Campos a actualizar.
     */
    void updateConversationUserStory(String conversationId, Map<String, Object> updateFields);

    /**
     * Elimina todos los conversaciones de la colección.
     */
    void deleteHistoryConversation();

    /**
     * Guarda o actualiza la información del usuario en la colección 'users'.
     * @param userData Datos del usuario.
     */
    void saveUser(Map<String, Object> userData);

    /**
     * Recupera todos los usuarios de la colección 'users'.
     * @return Lista de usuarios.
     */
    List<Map<String, Object>> getAllUsers();

    /**
     * Encuentra un documento en la colección especificada que coincida con el query dado.
     * @param collection Nombre de la colección.
     * @param query Diccionario de consulta.
     * @return Documento encontrado o null si no se encuentra.
     */
    Map<String, Object> findOne(String collection, Map<String, Object> query);
}