package org.acme.domain.minio.model;

public class FileUploadResponse {
    private String fileUrl;
    private String fileName;
    private String fileType;
    private String bucketName;

    // Constructores
    public FileUploadResponse() {}
    
    public FileUploadResponse(String fileUrl, String fileName, String fileType, String bucketName) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
        this.bucketName = bucketName;
    }

    // Getters y setters
    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
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

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}