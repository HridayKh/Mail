package in.HridayKh.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

enum FolderType {
	INBOX, SENT, DRAFTS, TRASH, SPAM, CUSTOM
}

@Entity
@Table(name = "folders")
public class Folder extends PanacheEntity {
	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FolderType type;

	@Column(nullable = false)
	private String colorHex;

}