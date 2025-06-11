package org.acme.domain.textextraction.model;

public class SourceRequest {
    private String type;
    private String media_type;
    private String file_name;
    private String data;
    
    // Constructor vacío para serialización
    public SourceRequest() {}
    
    public SourceRequest(String type, String media_type, String file_name, String data) {
        this.type = type;
        this.media_type = media_type;
        this.file_name = file_name;
        this.data = data;
    }
    
    // Getters y setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMedia_type() {
        return media_type;
    }
    
    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }
    
    public String getFile_name() {
        return file_name;
    }
    
    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
}