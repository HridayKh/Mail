package smtp;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import com.mailgun.util.Constants;

import io.sentry.Sentry;
import models.EmailRequest;
import models.SendMailResponse;

/**
 * Simple Mailgun email sender. Downloads attachments from URLs and attaches them.
 */
public class SendMail {
	private static final Logger log = LogManager.getLogger(SendMail.class);
	private static final String MAILGUN_KEY = System.getenv("MAILGUN_KEY");
	private static final String DOMAIN = "hridaykh.in";

	public static SendMailResponse send(EmailRequest req) {
		// Validate required fields
		if (req.getTo() == null || req.getTo().isEmpty()) {
			log.error("Recipient list (to) is required and cannot be empty.");
			return new SendMailResponse(null, "failed", Instant.now().toString());
		}

		try {
			// Build the message
			Message.MessageBuilder builder = Message.builder()
				.from(req.getFrom())
				.to(req.getTo())
				.cc(req.getCc())
				.bcc(req.getBcc())
				.subject(req.getSubject())
				.html(req.getBody());

			// Download and attach files (if any)
			List<File> tempFiles = new ArrayList<>();
			for (var att : req.getAttachments()) {
				File f = att.getFileObj();
				if (f != null) tempFiles.add(f);
			}
			if (!tempFiles.isEmpty()) builder.attachment(tempFiles);

			// Send the message
			MailgunMessagesApi api = MailgunClient.config(Constants.EU_REGION_BASE_URL, MAILGUN_KEY)
				.createApi(MailgunMessagesApi.class);
			MessageResponse resp = api.sendMessage(DOMAIN, builder.build());

			// Clean up temp files
			for (File f : tempFiles) if (f.exists()) f.delete();

			if (resp != null && resp.getId() != null) {
				log.info("Mailgun sent: id={}, message={}", resp.getId(), resp.getMessage());
				return new SendMailResponse(resp.getId(), "queued", Instant.now().toString());
			} else {
				log.error("Mailgun response invalid: {}", resp);
				return new SendMailResponse(null, "failed", Instant.now().toString());
			}
		} catch (Exception e) {
			log.error("Mailgun Send Failed", e);
			Sentry.captureException(e);
			return new SendMailResponse(null, "failed", Instant.now().toString());
		}
	}

	// Download a file from a URL to a temp file. Returns null on failure.
	public static File downloadToTemp(String urlStr) {
		if (urlStr == null || urlStr.isEmpty()) return null;
		try {
			URL url = new URI(urlStr).toURL();
			File temp = File.createTempFile("mailgun_attach_", null);
			try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(temp)) {
				in.transferTo(out);
			}
			return temp;
		} catch (Exception e) {
			log.error("Failed to download attachment: {}", urlStr, e);
			Sentry.captureException(e);
			return null;
		}
	}
}
