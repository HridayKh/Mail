package in.HridayKh.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "domains")
public class Domain extends PanacheEntity {
	@Column(unique = true, nullable = false)
	public String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public MailgunReigon mailgunRegion;

	@CreationTimestamp
	public LocalDateTime createdAt;
}
