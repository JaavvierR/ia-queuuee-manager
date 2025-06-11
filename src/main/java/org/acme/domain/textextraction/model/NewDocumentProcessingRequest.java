package org.acme.domain.textextraction.model;

public class NewDocumentProcessingRequest {
    private String file_type;
    private SourceRequest source;
    
    // Constructor vacío para serialización
    public NewDocumentProcessingRequest() {}
    
    public NewDocumentProcessingRequest(String file_type, SourceRequest source) {
        this.file_type = file_type;
        this.source = source;
    }
    
    // Getters y setters
    public String getFile_type() {
        return file_type;
    }
    
    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }
    
    public SourceRequest getSource() {
        return source;
    }
    
    public void setSource(SourceRequest source) {
        this.source = source;
    }
    
    // Método para convertir al formato anterior para compatibilidad
    public DocumentProcessingRequest toDocumentProcessingRequest() {
        return new DocumentProcessingRequest(
            this.source.getData(),
            this.source.getFile_name(),
            this.file_type,
            "tesseract_es" // Por defecto español, se podría agregar como parámetro si es necesario
        );
    }
}
