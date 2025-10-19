package models;

public class SendMailResponse {
    private String id;
    private String status;
    private String timestamp;

    public SendMailResponse(String id, String status, String timestamp) {
        this.id = id;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
