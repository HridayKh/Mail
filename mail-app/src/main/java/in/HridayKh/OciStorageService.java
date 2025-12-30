package in.HridayKh;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

import org.jboss.resteasy.reactive.server.multipart.FormValue;

@ApplicationScoped
public class OciStorageService {

	@Inject
	S3Presigner presigner;

	@Inject
	S3Client s3;

	private final String bucketName = "mail-app-attachments";

	public String generateDownloadUrl(String objectKey, int expirationInHours) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofHours(expirationInHours))
				.getObjectRequest(getObjectRequest)
				.build();

		PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

		return presignedRequest.url().toString();
	}

	public String uploadToOCI(FormValue value, String requestId) {
		String datePath = java.time.LocalDate.now().toString(); // e.g., 2025-12-30
		String uniqueId = java.util.UUID.randomUUID().toString();
		String safeFileName = value.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_");

		// Final key: attachments/2025-12-30/email_EMAIL-ID/a1b2c3d4-document.pdf
		String objectKey = String.format("attachments/%s/%s/%s-%s",
				datePath, requestId, uniqueId, safeFileName);

		PutObjectRequest putRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.contentType(value.getHeaders().getFirst("Content-Type"))
				.build();

		PutObjectResponse putResponse = s3.putObject(putRequest,
				RequestBody.fromFile(value.getFileItem().getFile()));

		return putResponse != null ? objectKey : null;
	}

}