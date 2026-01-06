package in.HridayKh.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "attachments")
public class Attachment extends PanacheEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "email_id", nullable = false)
	public Email email;

	@Column(nullable = false)
	public String fileName;

	@Column(nullable = false)
	public String fileUrl;

	@Column(nullable = false)
	public String mimeType;

	@Column(nullable = false)
	public Long fileSizeBytes;

	public String toFancyString() {
		return id + ": " + fileName + " (" + mimeType + ") " + fileSizeBytes +
				"B email(" + (email != null ? email.id : "null") + ") " + fileUrl;
	}

}