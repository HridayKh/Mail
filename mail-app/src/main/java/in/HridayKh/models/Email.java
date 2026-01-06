package in.HridayKh.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emails")
public class Email extends PanacheEntity {

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String separator = "--------------------------------------------------";

		sb.append("\n").append(separator).append("\n");
		sb.append(String.format("📧 EMAIL [ID: %d] | Status: %s\n", id, status));
		sb.append(separator).append("\n");

		sb.append(String.format("From:    %s\n", fromAddress));
		sb.append(String.format("To:      %s\n", toEmail));

		if (cc != null && !cc.isEmpty())
			sb.append(String.format("CC:      %s\n", cc));

		sb.append(String.format("Subject: %s\n", subject));
		sb.append(String.format("Date:    %s\n", timestamp));

		sb.append(separator).append("\n");

		// Preview the body (first 50 chars)
		String preview = (textBody != null && textBody.length() > 50)
				? textBody.substring(0, 50).replace("\n", " ") + "..."
				: textBody;
		sb.append(String.format("Preview: %s\n", preview));

		sb.append(separator).append("\n");

		// Metadata Flags
		sb.append(String.format("Flags:   [Read: %b] [Archived: %b] [Deleted: %b]\n", isRead, isArchived,
				isDeleted));
		sb.append(String.format("Folder:  %s\n", folder != null ? folder.id : "None"));

		if (attachments != null && !attachments.isEmpty()) {
			sb.append(String.format("Files:   %d attachment(s)\n", attachments.size()));
		}

		sb.append(separator).append("\n");

		return sb.toString();
	}

	@Column(nullable = false)
	public String subject;

	@Column(columnDefinition = "TEXT", nullable = false)
	public String htmlBody;

	@Column(columnDefinition = "TEXT")
	public String textBody;

	@Column(nullable = false)
	public String fromAddress;

	@Column(nullable = false)
	public String fromEmail;

	@Column(nullable = false)
	public String fromName;

	// Sender Info
	@Column(nullable = false)
	public String senderAddress;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn()
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	public SenderIdentity senderIdentity;

	@Column(nullable = false)
	public LocalDateTime timestamp;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public EmailStatus status; // DRAFT, SCHEDULED, SENT, FAILED, RECEIVED

	@Column(columnDefinition = "TEXT")
	public String unsubscribeHeader;

	// Mailgun specific
	@Column(unique = true)
	public String mailgunMessageId;

	@Column()
	public String inReplyToMessageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn()
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentEmail", "attachments"})
	public Email parentEmail;

	// Recipients
	@Column(columnDefinition = "TEXT", nullable = false)
	public String toEmail;

	@Column()
	public String cc;

	@Column()
	public String bcc;

	@Column
	public boolean isRead;

	@Column
	public boolean isArchived;

	@Column
	public boolean isDeleted;

	@ManyToOne
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	public Folder folder;

	@OneToMany(mappedBy = "email", cascade = CascadeType.ALL)
	@JsonIgnoreProperties({"email"})
	public List<Attachment> attachments = new ArrayList<>();

}