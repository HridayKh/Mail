package in.HridayKh.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "folders")
public class Folder extends PanacheEntity {
	@Column(nullable = false)
	public String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public FolderType type;

	@Column(nullable = false)
	public String colorHex;

}