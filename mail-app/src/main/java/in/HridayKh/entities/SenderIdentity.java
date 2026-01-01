package in.HridayKh.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sender_identities")
public class SenderIdentity extends PanacheEntity {

	@ManyToOne
	@JoinColumn(name = "domain_id", nullable = false)
	@JsonIgnore
	public Domain domain;

	@Column(nullable = false)
	public String email;

	@Column(nullable = false)
	public String displayName;

	@Column
	public boolean isDefault;
}
