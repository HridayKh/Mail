package in.HridayKh.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "domains")
public class Domain extends PanacheEntity {
	@Column(unique = true, nullable = false)
	private String name;

	@Column(nullable = false)
	private boolean isVerified;

	@Column(nullable = false)
	private String mailgunRegion;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
