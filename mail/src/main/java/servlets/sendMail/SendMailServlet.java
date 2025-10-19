package servlets.sendMail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import models.Attachment;
import models.EmailRequest;
import models.SendMailResponse;
import smtp.SendMail;
import utils.HttpUtil;

public class SendMailServlet {
    private static final Logger log = LogManager.getLogger(SendMailServlet.class);

    public static void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        try {
            EmailRequest emailRequest;
            List<File> uploadedFiles = new ArrayList<>();

            if (isMultipart(req)) {
                // Parse multipart: JSON fields + file parts
                String jsonString = null;
                for (Part part : req.getParts()) {
                    if (part.getName().equals("json")) {
                        try (InputStream in = part.getInputStream()) {
                            jsonString = new String(in.readAllBytes());
                        }
                    } else if (part.getContentType() != null && part.getSize() > 0) {
                        // Save uploaded file to temp file
                        File temp = File.createTempFile("upload_", null);
                        try (InputStream in = part.getInputStream();
                             FileOutputStream out = new FileOutputStream(temp)) {
                            in.transferTo(out);
                        }
                        uploadedFiles.add(temp);
                    }
                }
                JSONObject requestJson = jsonString != null ? new JSONObject(jsonString) : new JSONObject();
                emailRequest = EmailRequest.fromJson(requestJson);
            } else {
                // Standard JSON POST
                JSONObject requestJson = HttpUtil.readBodyJSON(req);
                emailRequest = EmailRequest.fromJson(requestJson);
            }

            // Compose all attachments: uploaded files + JSON attachments (possibly remote URLs)
            List<Attachment> combined = new ArrayList<>();
            if (emailRequest.getAttachments() != null) {
                for (Attachment a : emailRequest.getAttachments()) {
                    // If File not set but URL is, download it
                    if (a.getFileObj() == null && a.getUrl() != null) {
                        File downloaded = SendMail.downloadToTemp(a.getUrl());
                        if (downloaded != null) a.setFileObj(downloaded);
                    }
                    combined.add(a);
                }
            }

            // Add directly uploaded files
            for (File f : uploadedFiles) {
                Attachment a = new Attachment();
                a.setFilename(f.getName());
                a.setFileObj(f);
                combined.add(a);
            }
            emailRequest.setAttachments(combined);

            SendMailResponse response = SendMail.send(emailRequest);

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("id", response.getId() != null ? response.getId() : JSONObject.NULL);
            jsonResponse.put("status", response.getStatus());
            jsonResponse.put("timestamp", response.getTimestamp());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse.toString());
        } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage(), e);
            Sentry.captureException(e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject errorResponse = new JSONObject();
            try {
                errorResponse.put("id", JSONObject.NULL);
                errorResponse.put("status", "failed");
                errorResponse.put("timestamp", java.time.Instant.now().toString());
                resp.getWriter().write(errorResponse.toString());
            } catch (org.json.JSONException | java.io.IOException ex) {
                log.error("Failed to write error response JSON", ex);
                Sentry.captureException(ex);
            }
        }
    }

    // Detect if the request is multipart (file upload)
    private static boolean isMultipart(HttpServletRequest req) {
        String ct = req.getContentType();
        return ct != null && ct.toLowerCase().startsWith("multipart/");
    }
}