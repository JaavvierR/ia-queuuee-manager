package org.acme.domain.textextraction.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SourceResponse {
    private String data;
    private String media_type;
    private String status;
    private String type;
    
    // Constructor vacío para serialización
    public SourceResponse() {}
    
    public SourceResponse(String data, String media_type, String status, String type) {
        this.data = data;
        this.media_type = media_type;
        this.status = status;
        this.type = type;
    }
    
    // Getters y setters
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getMedia_type() {
        return media_type;
    }
    
    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Método auxiliar para obtener el texto extraído directamente
     * @return El texto extraído del documento
     */
    @JsonIgnore
    public String getText() {
        return this.data;
    }
}