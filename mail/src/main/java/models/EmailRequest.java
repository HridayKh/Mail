package models;

import java.util.List;

import org.json.JSONObject;

/**
 * EmailRequest holds email fields and provides sensible defaults and helpers
 * to build an instance from JSON input.
 */
public class EmailRequest {
    private String from = "Hriday Khanna <mail@HridayKh.in>";
    private List<String> to = null;
    private List<String> cc = null;
    private List<String> bcc = null;
    private String subject = "empty subject";
    private String body = "";
    private List<Attachment> attachments = null;

    public EmailRequest() {}

    public static EmailRequest fromJson(JSONObject json) {
        EmailRequest req = new EmailRequest();
        if (json == null) {
            return req;
        }

        req.from = json.optString("from", req.from);
        req.subject = json.optString("subject", req.subject);
        req.body = json.optString("body", req.body);

        req.to = utils.EmailRequestUtil.jsonArrayToStringList(json.optJSONArray("to"));
        req.cc = utils.EmailRequestUtil.jsonArrayToStringList(json.optJSONArray("cc"));
        req.bcc = utils.EmailRequestUtil.jsonArrayToStringList(json.optJSONArray("bcc"));
        req.attachments = utils.EmailRequestUtil.jsonArrayToAttachmentList(json.optJSONArray("attachments"));

        return req;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from != null ? from : "Hriday Khanna <mail@HridayKh.in>";
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject != null ? subject : "empty subject";
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body != null ? body : "";
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}