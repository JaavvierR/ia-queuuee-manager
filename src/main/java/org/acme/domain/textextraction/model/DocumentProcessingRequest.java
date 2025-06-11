package org.acme.domain.textextraction.model;

public class DocumentProcessingRequest {
    private String base64Content;
    private String fileName;
    private String fileType;
    private String language;
    
    // Constructor vacío para serialización
    public DocumentProcessingRequest() {}
    
    public DocumentProcessingRequest(String base64Content, String fileName, String fileType, String language) {
        this.base64Content = base64Content;
        this.fileName = fileName;
        this.fileType = fileType;
        this.language = language != null ? language : "tesseract_es";
    }
    
    // Getters y setters
    public String getBase64Content() {
        return base64Content;
    }
    
    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    private String mediaType;
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
}