package models;

import java.io.File;

public class Attachment {
    private String filename;
    private File fileObj;
    private String url; // Added for remote attachments

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public File getFileObj() {
        return fileObj;
    }

    public void setFileObj(File fileObj) {
        this.fileObj = fileObj;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}