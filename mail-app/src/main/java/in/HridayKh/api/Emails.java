package in.HridayKh.api;

import in.HridayKh.entities.Email;
import in.HridayKh.entities.Attachment;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/v1/emails")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Emails {

	@GET
	public List<Email> listEmails() {
		return Email.listAll();
	}

	@GET
	@Path("/{email_id}")
	public Email getEmail(@PathParam("email_id") Long emailId) {
		return Email.findById(emailId);
	}

	@GET
	@Path("/{email_id}/attachments")
	public List<Attachment> getAttachments(@PathParam("email_id") Long emailId) {
		Email email = Email.findById(emailId);
		if (email == null)
			throw new NotFoundException();
		return email.attachments;
	}

	@GET
	@Path("/{email_id}/attachments/{attachment_id}")
	public Attachment getAttachment(@PathParam("email_id") Long emailId,
			@PathParam("attachment_id") Long attachmentId) {
		Email email = Email.findById(emailId);
		if (email == null)
			throw new NotFoundException();
		return email.attachments.stream().filter(a -> a.id.equals(attachmentId)).findFirst()
				.orElseThrow(NotFoundException::new);
	}

	@GET
	@Path("/{email_id}/raw")
	@Produces(MediaType.TEXT_PLAIN)
	public String getRawEmail(@PathParam("email_id") Long emailId) {
		Email email = Email.findById(emailId);
		if (email == null)
			throw new NotFoundException();
		return email.textBody != null ? email.textBody : email.htmlBody;
	}

	@DELETE
	@Path("/{email_id}")
	public Response deleteEmail(@PathParam("email_id") Long emailId) {
		boolean deleted = Email.deleteById(emailId);
		if (!deleted)
			throw new NotFoundException();
		return Response.noContent().build();
	}
}
