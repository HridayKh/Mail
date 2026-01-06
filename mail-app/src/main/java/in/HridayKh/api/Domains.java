package in.HridayKh.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.HridayKh.models.Domain;
import in.HridayKh.utils.HttpUtil;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/domains")
public class Domains {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Domain> listDomains() {
		return Domain.listAll();
	}

	@POST
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createDomains(Domain domain) {
		if (domain == null || domain.name == null || domain.name.isBlank())
			return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "empty Domain name not allowed");
		domain.name = domain.name.toLowerCase();

		if (Domain.find("name", domain.name).firstResult() != null)
			return HttpUtil.errorResponse(Response.Status.CONFLICT,
					"Domain with name " + domain.name + " already exists.");

		domain.persist();

		return Response.status(Response.Status.CREATED).entity(domain).build();
	}

	@GET
	@Path("/{domain_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDomain(@PathParam("domain_id") String domainId) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);
			return Response.ok(domain).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

	@PATCH
	@Path("/{domain_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDomain(@PathParam("domain_id") String domainId, Domain update) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);

			if (update == null)
				return HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "Request body is required");

			updateNameIfPresent(domain, update);

			if (update.mailgunRegion != null)
				domain.mailgunRegion = update.mailgunRegion;

			domain.persist();
			return Response.ok(domain).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

	@DELETE
	@Path("/{domain_id}")
	@Transactional
	public Response deleteDomain(@PathParam("domain_id") String domainId) {
		try {
			Domain domain = HttpUtil.requireDomain(domainId);

			Map<String, Object> resp = new HashMap<>();
			resp.put("createdAt", domain.createdAt != null ? domain.createdAt.toString() : null);
			resp.put("mailgunRegion",
					domain.mailgunRegion != null ? domain.mailgunRegion.toString() : null);
			resp.put("name", domain.name);
			resp.put("id", domain.id);
			resp.put("status", "deleted");

			domain.delete();
			return Response.ok(resp).build();
		} catch (WebApplicationException e) {
			return e.getResponse();
		}
	}

	private void updateNameIfPresent(Domain domain, Domain update) {
		if (update.name == null)
			return;
		String newName = update.name.trim().toLowerCase();
		if (newName.isBlank())
			throw new WebApplicationException(
					HttpUtil.errorResponse(Response.Status.BAD_REQUEST, "empty Domain name not allowed"));

		Domain existing = Domain.find("name", newName).firstResult();
		if (existing != null && !existing.id.equals(domain.id))
			throw new WebApplicationException(
					HttpUtil.errorResponse(Response.Status.CONFLICT,
							"Domain with name " + newName + " already exists."));

		domain.name = newName;
	}

}
