package org.acme.domain.chatbot.model.conversation;

import java.util.UUID;
//import org.acme.domain.models.screening.chatbot.technicalReport.TechnicalReport;
import org.bson.codecs.pojo.annotations.BsonId;

public class Message {
    @BsonId
    private String id;
    private String role;
    private Object content;
    private boolean validate;
    //private TechnicalReport technicalReport; // Nuevo campo

    public Message() {
        this.id = UUID.randomUUID().toString();
    }

    public Message(String role, Object content) {
        this.id = UUID.randomUUID().toString();
        this.role = role;
        this.content = content;
    }

    public Message(String id, String role, Object content) {
        this.id = id;
        this.role = role;
        this.content = content;
    }

    public Message(String id, String role, Object content, boolean validate) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.validate = validate;
    }



    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }



    @Override
    public String toString() {
        String contentStr;
        
        // Si content es String, recorta el contenido a 20 caracteres
        if (content instanceof String) {
            contentStr = ((String) content).length() > 20
                     ? ((String) content).substring(0, 20) + "..."
                    : (String) content;
        } else {
            // Si content es otro tipo de objeto, usa su toString()
            contentStr = content != null ? content.toString() : "null";
        }
        
        return "Message{" +
                "id='" + id + '\'' +
                ", role='" + role + '\'' +
                ", content=" + contentStr +
                ", validate=" + validate +
                '}';
    }
}