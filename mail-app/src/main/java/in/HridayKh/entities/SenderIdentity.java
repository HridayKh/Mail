package in.HridayKh.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sender_identities")
public class SenderIdentity extends PanacheEntity {

	@ManyToOne
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String displayName;

	@Column
	private boolean isDefault;
}
