package smtp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import com.mailgun.util.EmailUtil;

// store sent message as sent in db
public class SendMail {
	private static final Logger log = LogManager.getLogger(SendMail.class);

	private static final String MAILGUN_KEY = System.getenv("MAILGUN_KEY");
	private static final String DOMAIN = "hridaykh.in";

	public static boolean send(String emailFrom, String nameFrom, String emailTo, String subject, String bodyHtml,
			String replyTo) {
		if (emailTo == null || emailTo.isEmpty() || !emailTo.contains("@")) {
			throw new IllegalArgumentException("Recipient email (emailTo) is required and must be valid.");
		}
		if (emailFrom == null || emailFrom.isEmpty()) {
			emailFrom = "no-reply@" + DOMAIN;
		}
		if (!emailFrom.contains("@")) {
			emailFrom = emailFrom + "@" + DOMAIN;
		}
		if (nameFrom == null || nameFrom.isEmpty()) {
			nameFrom = "No Name";
		}
		if (subject == null || subject.isEmpty()) {
			subject = "(No subject)";
		}
		if (bodyHtml == null || bodyHtml.isEmpty()) {
			bodyHtml = "";
		}
		if (replyTo == null || replyTo.isEmpty()) {
			replyTo = emailFrom;
		}

		MailgunMessagesApi mailgunMessagesApi = MailgunClient.config("https://api.eu.mailgun.net/", MAILGUN_KEY)
				.createApi(MailgunMessagesApi.class);

		Message message = Message.builder().from(EmailUtil.nameWithEmail(nameFrom, emailFrom)).to(emailTo)
				.subject(subject).html(bodyHtml).replyTo(replyTo).build();

		MessageResponse messageResponse = null;
		try {
			messageResponse = mailgunMessagesApi.sendMessage(DOMAIN, message);
			if (messageResponse != null && messageResponse.getId() != null && messageResponse.getMessage() != null) {
				log.info("Mailgun sent: id={}, message={}", messageResponse.getId(), messageResponse.getMessage());
				return true;
			} else {
				log.error("Mailgun response invalid: {}", messageResponse);
				return false;
			}
		} catch (Exception e) {
			log.error("Mailgun send failed", e);
			return false;
		}
	}
}
