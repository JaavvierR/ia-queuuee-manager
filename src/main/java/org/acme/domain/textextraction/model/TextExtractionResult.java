package org.acme.domain.textextraction.model;

public class TextExtractionResult {
    private String file_name;
    private long file_size;
    private String file_type;
    private String file_url;
    private SourceResponse source;
    
    // Constructor vacío para serialización
    public TextExtractionResult() {}
    
    public TextExtractionResult(String file_name, long file_size, String file_type,
                              String file_url, SourceResponse source) {
        this.file_name = file_name;
        this.file_size = file_size;
        this.file_type = file_type;
        this.file_url = file_url;
        this.source = source;
    }
    
    // Getters y setters
    public String getFile_name() {
        return file_name;
    }
    
    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
    
    public long getFile_size() {
        return file_size;
    }
    
    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }
    
    public String getFile_type() {
        return file_type;
    }
    
    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }
    
    public String getFile_url() {
        return file_url;
    }
    
    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }
    
    public SourceResponse getSource() {
        return source;
    }
    
    public void setSource(SourceResponse source) {
        this.source = source;
    }
}