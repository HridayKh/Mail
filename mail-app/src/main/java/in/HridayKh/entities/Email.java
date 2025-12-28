package in.HridayKh.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

enum EmailStatus {
	DRAFT, SCHEDULED, SENT, FAILED
}

@Entity
@Table(name = "emails", indexes = {
	@Index(name = "idx_sent_at", columnList = "sentAt"),
	@Index(name = "idx_thread", columnList = "threadId")
})
public class Email extends PanacheEntity {

	@Column(nullable = false)
	private String subject;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String htmlBody;

	@Column(columnDefinition = "TEXT")
	private String textBody;

	// Sender Info
	@Column(nullable = false)
	private String fromAddress; // Full string: "Sender Name <sender@example.com>"

	@Column(nullable = false)
	private String fromEmail; // Just email: "sender@example.com"

	@Column(nullable = false)
	private String fromName; // Just name: "Sender Name"

	// If OUTGOING, this links to the identity used
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_identity_id")
	private SenderIdentity senderIdentity;

	@Column(nullable = false)
	private LocalDateTime sentAt;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EmailStatus status = EmailStatus.DRAFT; // DRAFT, SCHEDULED, SENT, FAILED

	@Column(columnDefinition = "TEXT")
	private String unsubscribeHeader;

	// Mailgun specific
	@Column(unique = true)
	private String mailgunMessageId;

	// Threading
	@Column
	private String threadId;

	@Column(name = "in_reply_to_message_id")
	private String inReplyToMessageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_email_id")
	private Email parentEmail;

	// Recipients
	@Column(columnDefinition = "TEXT")
	private String toEmail; // Comma-separated addresses: "a@b.com, c@d.com"

	@Column(columnDefinition = "JSON")
	private String ccJson;

	@Column(columnDefinition = "JSON")
	private String bccJson;

	@OneToMany(mappedBy = "email", cascade = CascadeType.ALL)
	private List<Attachment> attachments = new ArrayList<>();

	// Folder/State Management (Simplified for single user)
	@ManyToOne
	private Folder folder;

	@Column
	private boolean isRead = false;

	@Column
	private boolean isArchived = false;

	@Column
	private boolean isDeleted = false;
}