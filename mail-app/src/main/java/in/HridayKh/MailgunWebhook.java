package in.HridayKh;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@jakarta.ws.rs.Path("/webhooks/mailgun")
public class MailgunWebhook {
	private static final Logger log = Logger.getLogger(MailgunWebhook.class);

	// Define your storage location
	private static final Path STORAGE_ROOT = Path.of("uploads");

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response handleDynamic(MultipartFormDataInput input) {
		try {
			// Create unique directory for this specific webhook call
			String requestId = UUID.randomUUID().toString();
			Path requestDir = STORAGE_ROOT.resolve(requestId);
			Files.createDirectories(requestDir);

			StringBuilder fieldLog = new StringBuilder();
			Map<String, Collection<FormValue>> map = input.getValues();

			for (var entry : map.entrySet()) {
				String key = entry.getKey();
				for (FormValue value : entry.getValue()) {
					if (value.isFileItem()) {
						saveFile(requestDir, key, value);
					} else {
						fieldLog.append(key).append(": ").append(value.getValue()).append("\n");
					}
				}
			}

			// Save all text fields to a single metadata file
			Files.writeString(requestDir.resolve("fields.txt"), fieldLog.toString());

			log.infof("Webhook processed. Files saved to: %s", requestDir.toAbsolutePath());
			return Response.ok("Saved to " + requestId).build();

		} catch (IOException e) {
			log.error("Failed to save webhook data", e);
			return Response.serverError().entity("Error saving data").build();
		}
	}

	private void saveFile(Path dir, String fieldName, FormValue value) throws IOException {
		String fileName = value.getFileName();
		if (fileName == null || fileName.isEmpty()) {
			fileName = "untitled_" + fieldName;
		}

		Path targetPath = dir.resolve(fieldName + " " + fileName);
		log.infof("Saving file: %s", fileName);

		// RESTEasy Reactive provides a File object or Path for the temporary upload
		Files.copy(value.getFileItem().getFile(), targetPath, StandardCopyOption.REPLACE_EXISTING);
	}
}