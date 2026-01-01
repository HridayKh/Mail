package in.HridayKh;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import in.HridayKh.entities.Attachment;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/test")
public class Test {

	@Inject
	OciStorageService ociStorageService;

	@GET
	@Path("/presigned-url/{attId}")
	public String testPresignedUrl(
			@PathParam("attId") String attId) {
		try {
			Long id = Long.parseLong(attId);
			Attachment attachment = Attachment.findById(id);
			return attachment != null ? ociStorageService.generateDownloadUrl(attachment.fileUrl, 1)
					: "Attachment not found";
		} catch (NumberFormatException e) {
			return "Invalid attachment ID format: must be a number";
		}
	}

	@GET
	@Path("/object-keys")
	public String getObjectKeys() {
		List<Attachment> attachments = Attachment.listAll();
		return attachments.stream()
				.map(a -> a.toFancyString())
				.reduce((a, b) -> a + "\n" + b)
				.orElse("No attachments found");
	}
}
