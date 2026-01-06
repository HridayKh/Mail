package in.HridayKh.api;

import java.util.List;
import java.util.Map;

import in.HridayKh.models.Domain;
import in.HridayKh.models.SenderIdentity;
import in.HridayKh.utils.HttpUtil;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/domains/{domain_id}/senders")
public class Senders {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listForDomain(@PathParam("domain_id") String domainId) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);
			List<SenderIdentity> list = SenderIdentity.find("domain", domain).list();
			return Response.ok(list).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response create(@PathParam("domain_id") String domainId, SenderIdentity body) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);

			if (body == null || body.email == null || body.email.isBlank())
				return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "email is required");
			if (body.displayName == null || body.displayName.isBlank())
				return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "displayName is required");

			body.email = body.email.trim().toLowerCase();
			body.domain = domain;

			SenderIdentity existing = SenderIdentity.find("domain = ?1 and email = ?2", domain, body.email)
					.firstResult();
			if (existing != null)
				return HttpUtil.errorResponse(Response.Status.CONFLICT,
						"Sender with email " + body.email + " already exists for this domain");

			if (body.isDefault) {
				SenderIdentity prev = SenderIdentity.find("domain = ?1 and isDefault = true", domain)
						.firstResult();
				if (prev != null) {
					prev.isDefault = false;
					prev.persist();
				}
			}

			body.persist();
			return Response.status(Response.Status.CREATED).entity(body).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

	@GET
	@Path("/{sender_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("domain_id") String domainId, @PathParam("sender_id") String senderId) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);
			long id = Long.parseLong(senderId);
			SenderIdentity s = SenderIdentity.findById(id);
			if (s == null || s.domain == null || s.domain.id != domain.id)
				return HttpUtil.errorResponse(Response.Status.NOT_FOUND, "Sender not found");
			return Response.ok(s).build();
		} catch (NumberFormatException e) {
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Invalid sender id format");
		}
	}

	@PATCH
	@Path("/{sender_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response update(@PathParam("domain_id") String domainId, @PathParam("sender_id") String senderId,
			SenderIdentity body) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);
			long id = Long.parseLong(senderId);
			SenderIdentity s = SenderIdentity.findById(id);
			if (s == null || s.domain == null || s.domain.id != domain.id)
				return HttpUtil.errorResponse(Response.Status.NOT_FOUND, "Sender not found");

			if (body.displayName != null)
				s.displayName = body.displayName;
			if (body.email != null)
				s.email = body.email.trim().toLowerCase();
			if (body.isDefault) {
				SenderIdentity prev = SenderIdentity.find("domain = ?1 and isDefault = true", domain)
						.firstResult();
				if (prev != null && !prev.id.equals(s.id)) {
					prev.isDefault = false;
					prev.persist();
				}
				s.isDefault = true;
			}
			s.persist();
			return Response.ok(s).build();
		} catch (NumberFormatException e) {
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Invalid sender id format");
		}
	}

	@DELETE
	@Path("/{sender_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response deleteForDomain(@PathParam("domain_id") String domainId,
			@PathParam("sender_id") String senderId) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);
			long id = Long.parseLong(senderId);
			SenderIdentity s = SenderIdentity.findById(id);
			if (s == null || s.domain == null || s.domain.id != domain.id)
				return HttpUtil.errorResponse(Response.Status.NOT_FOUND, "Sender not found");
			s.delete();
			return Response.ok(Map.of("status", "deleted", "id", id)).build();
		} catch (NumberFormatException e) {
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Invalid sender id format");
		}
	}

}
