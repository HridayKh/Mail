package in.HridayKh.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment extends PanacheEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "email_id", nullable = false)
	private Email email;

	@Column(nullable = false)
	private String fileName;

	@Column(nullable = false)
	private String mimeType;

	@Column(nullable = false)
	private Long fileSizeBytes;

	@Column(nullable = false)
	private String fileUrl;

	@CreationTimestamp
	private LocalDateTime createdAt;
}