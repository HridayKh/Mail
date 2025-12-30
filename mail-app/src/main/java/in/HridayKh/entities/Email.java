package in.HridayKh.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emails")
public class Email extends PanacheEntity {

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
	public Folder folder;

	@OneToMany(mappedBy = "email", cascade = CascadeType.ALL)
	public List<Attachment> attachments = new ArrayList<>();

}