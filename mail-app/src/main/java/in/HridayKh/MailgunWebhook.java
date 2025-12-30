package in.HridayKh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.HridayKh.entities.Attachment;
import in.HridayKh.entities.Email;
import in.HridayKh.entities.EmailStatus;
import in.HridayKh.entities.Folder;
import in.HridayKh.entities.FolderType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

@Path("/webhooks/mailgun")
public class MailgunWebhook {
	private static final Logger log = Logger.getLogger(MailgunWebhook.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	private Map<String, Collection<FormValue>> map = null;

	@Inject
	OciStorageService ociStorageService;

	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response handleWebhook(MultipartFormDataInput input) {
		try {
			this.map = input.getValues();

			String mailgunMessageId = getVal("Message-Id");
			Email existing = Email.find("mailgunMessageId", mailgunMessageId).firstResult();
			if (existing != null) {
				log.infof("Email with Mailgun Message ID %s already exists. Skipping...",
						mailgunMessageId);
				return Response.ok("Email already exists with ID: " + existing.mailgunMessageId)
						.build();
			}

			Email email = new Email();

			email.subject = getVal("subject");
			email.textBody = getVal("body-plain");
			email.htmlBody = getVal("body-html");
			email.fromAddress = getVal("from");
			email.senderAddress = getVal("sender");
			email.senderIdentity = null;
			email.mailgunMessageId = mailgunMessageId;

			email.toEmail = getVal("To");
			email.cc = getVal("recipient");
			email.bcc = "";

			email.isRead = false;
			email.isArchived = false;
			email.isDeleted = false;

			email.folder = ensureInboxExists();

			String rawFrom = email.fromAddress;
			if (rawFrom != null && rawFrom.contains("<")) {
				email.fromName = rawFrom.substring(0, rawFrom.indexOf("<")).trim();
				email.fromEmail = rawFrom.substring(rawFrom.indexOf("<") + 1, rawFrom.indexOf(">"))
						.trim();
			} else {
				email.fromEmail = rawFrom;
				email.fromName = "";
			}
			String rawDate = getVal("Date");
			email.timestamp = rawDate != null
					? ZonedDateTime.parse(rawDate, DateTimeFormatter.RFC_1123_DATE_TIME)
							.toLocalDateTime()
					: LocalDateTime.now();

			email.status = EmailStatus.RECEIVED;

			email.unsubscribeHeader = null;
			for (JsonNode header : mapper.readTree(getVal("message-headers")))
				if (header.isArray() && header.size() >= 2)
					if ("List-Unsubscribe".equalsIgnoreCase(header.get(0).asText()))
						email.unsubscribeHeader = header.get(1).asText();

			String inReplyTo = getVal("In-Reply-To");
			email.inReplyToMessageId = inReplyTo;
			email.parentEmail = null;
			if (inReplyTo != null && !inReplyTo.isBlank()) {
				Email rootEmail = findRootEmail(inReplyTo);
				if (rootEmail != null)
					email.parentEmail = rootEmail;
			}

			if (!handleAttachments(email)) {
				log.error("Failed to handle one or more attachments");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity("Failed to handle attachments").build();
			}

			email.persist();
			return Response.ok("Saved Email ID: " + email.mailgunMessageId).build();

		} catch (Exception e) {
			log.error("Failed to process email", e);
			return Response.serverError().build();
		}
	}

	private boolean handleAttachments(Email email) throws IOException {
		String requestId = "email_" + email.mailgunMessageId;
		for (var entry : map.entrySet()) {
			for (FormValue value : entry.getValue()) {
				if (value.isFileItem()) {
					String objectKey = ociStorageService.uploadToOCI(value, requestId);
					if (objectKey == null) {
						log.errorf("Failed to upload attachment %s to OCI",
								value.getFileName());
						return false;
					}
					Attachment att = new Attachment();
					att.email = email;
					att.fileName = value.getFileName();

					att.fileUrl = objectKey;

					att.mimeType = value.getHeaders().getFirst("Content-Type");

					att.fileSizeBytes = value.getFileItem().getFileSize();

					email.attachments.add(att);

					log.infof("Uploaded attachment %s to OCI folder %s", att.fileName,
							requestId);

				}
			}
		}
		return true;
	}

	private Email findRootEmail(String inReplyTo) {
		Email current = Email.find("mailgunMessageId", inReplyTo).firstResult();
		if (current == null)
			return null;
		Email root = current;
		while (root.inReplyToMessageId != null && !root.inReplyToMessageId.isBlank()) {
			Email nextUp = Email.find("mailgunMessageId", root.inReplyToMessageId).firstResult();
			if (nextUp == null)
				break;
			root = nextUp;
		}
		return root;
	}

	private Folder ensureInboxExists() {
		Folder inbox = Folder.find("type", FolderType.INBOX).firstResult();
		if (inbox == null) {
			log.info("No Inbox folder found. Creating default Inbox...");
			inbox = new Folder();
			inbox.name = "Inbox";
			inbox.type = FolderType.INBOX;
			inbox.colorHex = "#3498db";
			inbox.persist();
		}
		return inbox;
	}

	private String getVal(String key) {
		Collection<FormValue> values = map.get(key);
		if (values == null || values.isEmpty())
			return null;
		return values.iterator().next().getValue();
	}

}
